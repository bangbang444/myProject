package bangbang.gourmet.crawler;

import bangbang.gourmet.restaurant.dto.RestaurantCrawledDto;
import bangbang.gourmet.restaurant.service.RestaurantService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverCrawler {

    public static final String SEARCH_URL = "https://map.naver.com/p/search/강남역 맛집";
    private final RestaurantService restaurantService;

    public void crawl() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(150));

            int currentPage = 1;
            boolean hasNextPage = true;
            while(hasNextPage) {
                log.info(">>>> [{}페이지] 수집 시작 (새 세션 생성) <<<<", currentPage);
                try(BrowserContext context = createNewContext(browser)){
                    Page page = context.newPage();
                    // 1. 검색 페이지 접속
                    page.navigate(SEARCH_URL);
                    log.info("1. 접속 시도");
                    // [추가] 2. 페이지 로딩 완료(?c= 좌표 생성) 대기
                    // 이 좌표가 떠야 지도가 완전히 로드되고 클릭 이벤트가 활성화됩니다.
                    try {
                        log.info("지도가 완전히 로드될 때까지 대기 중...");
                        page.waitForURL(url -> url.contains("?c="), new Page.WaitForURLOptions().setTimeout(15000));
                    } catch (Exception e) {
                        log.warn("좌표 로딩 대기 중 타임아웃이 발생했으나 계속 진행합니다.");
                    }

                    // 3. 검색 결과 프레임 정의 및 첫 번째 식당 대기
                    FrameLocator searchFrame = page.frameLocator("#searchIframe");

                    if(currentPage > 1){
                        boolean pageFound = false;
                        // 목표 페이지로 이동
                        while(!pageFound){
                            // 다음 숫자 버튼(currentPage + 1)을 먼저 찾고, 없으면 [다음] 화살표를 찾습니다.
                            Locator nextNumBtn = searchFrame.locator("a.mBN2s").filter(new Locator.FilterOptions().setHasText(String.valueOf(currentPage)));
                            if(nextNumBtn.isVisible()){
                                nextNumBtn.click();
                                page.waitForTimeout(3000); // 페이지 전환 대기
                                pageFound=true;
                            }else{
                                Locator nextArrowBtn = searchFrame.locator("a.eUTV2").filter(new Locator.FilterOptions().setHas(searchFrame.locator("span:has-text('다음페이지')")));
                                if(nextArrowBtn.isVisible() && !"true".equals(nextArrowBtn.getAttribute("aria-disabled"))){
                                    nextArrowBtn.click();
                                    page.waitForTimeout(3000);
                                }else{
                                    hasNextPage = false;
                                    break;
                                }
                            }
                        }
                    }
                    if(!hasNextPage) continue;

                    // 요소가 실제로 화면에 나타날 때까지 대기
                    searchFrame.locator("a.place_bluelink").
                            first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

                    int processedCount = 0; // 지금까지 완료한 식당 수
                    while (true) {
                        Locator restaurantLinks = searchFrame.locator("a.place_bluelink");
                        int currentTotal = restaurantLinks.count();

                        if (processedCount >= currentTotal) {
                            log.info("현재 로드된 {}개를 모두 처리함. 추가 로딩을 위해 스크롤 합니다.", currentTotal);

                            // 마지막 요소로 스크롤 이동하여 로딩 트리거
                            restaurantLinks.last().scrollIntoViewIfNeeded();
                            page.waitForTimeout(1500); // 데이터 로드 대기

                            // 스크롤 후에도 개수가 그대로라면 이 페이지는 진짜 끝! (혹은 다음 페이지 버튼 필요)
                            if (searchFrame.locator("a.place_bluelink").count() == currentTotal) {
                                log.info("더 이상 추가되는 식당이 없습니다. 현재 페이지 수집 종료.");
                                break;
                            }
                            continue; // 개수가 늘어났으니 다시 위로 가서 수집 계속
                        }

                        try {
                            log.info("--- {}번째 식당 작업 시작 ---", processedCount + 1);

                            // i번째 식당을 선택해서 클릭
                            Locator target = restaurantLinks.nth(processedCount);
                            target.scrollIntoViewIfNeeded();
                            target.click();

                            // 상세 페이지 iframe 대기
                            FrameLocator detailFrame = page.frameLocator("#entryIframe");

                            // 식당 이름이 나타날 때까지 대기 (데이터 로딩 확인)
                            detailFrame.locator("#_title span.GHAhO").waitFor(new Locator.WaitForOptions().setTimeout(5000));

                            String title = detailFrame.locator("#_title span.GHAhO").innerText();
                            String category = detailFrame.locator("#_title span.lnJFt").innerText();
                            String address = detailFrame.locator("span.pz7wy").innerText(); // LDgIH
                            // 콤마로 분리하고 공백 제거하여 리스트화
                            List<String> categoryList = Arrays.stream(category.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList();

                            log.info("결과: {} / {} / {}", title, categoryList, address);

                            // 2. 실제 Frame 객체 획득 (안전한 방식)
                            Frame entryFrame = page.frames().stream()
                                    .filter(f -> "entryIframe".equals(f.name()) || f.url().contains("entryIframe"))
                                    .findFirst()
                                    .orElse(null);

                            String x = "0.0";
                            String y = "0.0";
                            if (entryFrame != null) {
                                // 3. 자바스크립트 실행하여 JSON 데이터 추출
                                String apolloStateJson = (String) entryFrame.evaluate("() => JSON.stringify(window.__APOLLO_STATE__)");

                                if (apolloStateJson != null && !apolloStateJson.equals("undefined")) {
                                    // 정규식으로 x(경도), y(위도) 추출
                                    x = extractCoordinate(apolloStateJson, "\"x\":\"(.*?)\"");
                                    y = extractCoordinate(apolloStateJson, "\"y\":\"(.*?)\"");

                                    log.info("좌표 데이터 획득 성공 -> x: {}, y: {}", x, y);
                                }
                            } else {
                                log.error("상세페이지 프레임을 찾을 수 없습니다.");
                            }

                            // 전화번호는 없을 수도 있으니 체크
                            String phoneNumber = "번호없음";
                            if (detailFrame.locator("span.xlx7Q").isVisible()) {
                                phoneNumber = detailFrame.locator("span.xlx7Q").innerText();
                            }

                            log.info("결과: {} / {} / {}", title, category, address);
                            Locator expandButton = detailFrame.locator("a[role='button'].gKP9i");

                            // 2. aria-expanded 상태를 체크하여 닫혀있을 때만 클릭
                            if ("false".equals(expandButton.getAttribute("aria-expanded"))) {
                                expandButton.click();
                                log.info("영업시간 상세 보기 버튼을 클릭했습니다.");
                            }
                            Locator days = detailFrame.locator("span.i8cJw");
                            Locator times = detailFrame.locator("div.H3ua4");

                            int dayCount = days.count();
                            int timeCount = times.count();
                            int finalCount = Math.min(dayCount, timeCount);

                            log.info("발견된 영업시간 항목 수: {}", finalCount);
                            List<RestaurantCrawledDto.OpeningHourDto> openingHourDtos = new ArrayList<>();
                            for (int j = 0; j < finalCount; j++) {
                                try {
                                    // 1. 요일 가져오기
                                    String dayText = days.nth(j).innerText(new Locator.InnerTextOptions().setTimeout(1000)).trim();

                                    // 2. 시간 가져오기 (evaluate를 사용하여 줄바꿈을 유지하며 가져옴)
                                    // 이 방식이 '11:10-21:4014:30'처럼 붙는 현상을 막아줍니다.
                                    String timeText = (String) times.nth(j).evaluate("el => el.innerText");

                                    // 3. 줄바꿈(\n)을 가독성 좋게 " | "로 변환
                                    if (timeText != null) {
                                        // 1. 시간 패턴 추출 로직
                                        Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}");

                                        // 줄 단위나 섹션 단위로 나누어 분석하는 것이 더 정확합니다.
                                        String[] lines = timeText.split("\n");

                                        String openTime = null;
                                        String closeTime = null;
                                        String breakTime = null;
                                        String lastOrders = ""; // 여러 개일 수 있으므로 문자열로 합침

                                        for (String line : lines) {
                                            Matcher matcher = timePattern.matcher(line);
                                            List<String> timesInLine = new ArrayList<>();
                                            while (matcher.find()) {
                                                timesInLine.add(matcher.group());
                                            }

                                            if (timesInLine.isEmpty()) continue;

                                            if (line.contains("브레이크타임")) {
                                                breakTime = String.join(" - ", timesInLine);
                                            } else if (line.contains("라스트오더")) {
                                                // "라스트오더: 21:30" 형태를 유지하며 추가
                                                if (!lastOrders.isEmpty()) lastOrders += " | ";
                                                lastOrders += String.join(", ", timesInLine);
                                            } else if (openTime == null && timesInLine.size() >= 2) {
                                                // 보통 첫 번째로 나오는 시간 쌍이 영업시간
                                                openTime = timesInLine.get(0);
                                                closeTime = timesInLine.get(1);
                                            }
                                        }

                                        // 2. 로그 출력
                                        log.info("정제데이터 -> 요일:{}, 시작: {}, 종료: {}, 브레이크: {}, 라스트오더: {}",
                                                dayText, openTime, closeTime, breakTime, lastOrders);

                                        openingHourDtos.add(RestaurantCrawledDto.OpeningHourDto.builder()
                                                .dayOfWeek(dayText)
                                                .startTime(openTime)
                                                .endTime(closeTime)
                                                .breakTime(breakTime)
                                                .lastOrder(lastOrders)
                                                .build());
                                    }

                                } catch (Exception e) {
                                    log.warn("{}번째 요일/시간 수집 중 일부 실패 (건너뜀)", j);
                                }
                            }

                            RestaurantCrawledDto restaurantDto = RestaurantCrawledDto.builder()
                                    .name(title)
                                    .categories(categoryList)
                                    .address(address)
                                    .phoneNumber(phoneNumber)
                                    .latitude(Double.parseDouble(y))
                                    .longitude(Double.parseDouble(x))
                                    .openingHours(openingHourDtos)
                                    .build();
                            restaurantService.saveCrawledData(restaurantDto);

                            processedCount++;

                            // 1. 매 식당 사이 랜덤 대기 (사람처럼 보이게)
                            long randomDelay = java.util.concurrent.ThreadLocalRandom.current().nextLong(4000, 6000);
                            log.info("--- {}번째 식당 저장 완료. 사람인 척 {}초 대기 중... ---", processedCount, randomDelay / 1000.0);
                            page.waitForTimeout(randomDelay);

                            // [병현 님 아이디어] 10개 단위로 잠시 휴식 (차단 방지)
                            if (processedCount % 10 == 0) {
                                log.info("10개 수집 완료. 3초간 휴식합니다.");
                                page.waitForTimeout(3000);
                            }

                        } catch (Exception e) {
                            log.error("{}번째 식당 처리 중 에러 발생: {}", processedCount + 1, e.getMessage());
                            // 에러가 나도 다음 식당으로 넘어가도록 continue 역할
                        }
                    } // 한 페이지 순회 완료 (while)

                    // 다음 페이지 존재 여부 확인 및 정보 업데이트
                    Locator nextNumBtn = searchFrame.locator("a.mBN2s").filter(new Locator.FilterOptions().setHasText(String.valueOf(currentPage + 1)));
                    Locator nextArrowBtn = searchFrame.locator("a.eUTV2").filter(new Locator.FilterOptions().setHas(searchFrame.locator("span:has-text('다음페이지')")));
                    if(nextNumBtn.isVisible() || (nextArrowBtn.isVisible() && !"true".equals(nextArrowBtn.getAttribute("aria-disabled")))){
                        currentPage++;
                    }else{
                        log.info("더 이상 이동할 페이지가 없습니다. 전체 수집 종료!");
                        hasNextPage = false; // 바깥쪽 while 루프 탈출
                    }
                }
            }
            browser.close();
        } catch (Exception e) {
            log.error("크롤링 중 에러 발생: ", e);
        }
    }

    private static BrowserContext createNewContext(Browser browser) {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"));
        return context;
    }

    /**
     * JSON 문자열에서 정규표현식을 이용해 좌표값을 추출합니다.
     */
    private String extractCoordinate(String json, String regex) {
        if (json == null) return "0.0";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0.0";
    }
}