package bangbang.gourmet.crawler;

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

import static bangbang.gourmet.crawler.NaverMapConstants.Common.*;
import static bangbang.gourmet.crawler.NaverMapConstants.Search.*;
import static bangbang.gourmet.crawler.NaverMapConstants.State.*;
import static bangbang.gourmet.crawler.NaverMapConstants.Detail.*;
import static bangbang.gourmet.crawler.NaverMapConstants.Pagination.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverCrawler { // TODO: 고정된 시간 대기 개선
    private final NaverCrawlerService naverCrawlerService;

    public void crawlAll(List<String> keywords) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(150));

            for (String keyword : keywords) {
                log.info("▶▶▶ [{}] 지역 수집을 시작합니다.", keyword);
                singleCrawl(browser, keyword);
            }

            browser.close();
        } catch (Exception e) {
            log.error("크롤링 중 에러 발생: ", e);
        }
    }

    private void singleCrawl(Browser browser, String keyword) {
        int currentPage = 1;
        boolean hasNextPage = true;
        while(hasNextPage) {
            log.info(">>>> [{}페이지] 수집 시작 (새 세션 생성) <<<<", currentPage);
            try(BrowserContext context = createNewContext(browser)){
                Page page = context.newPage();
                // 1. 페이지 초기화
                initSearchPage(page, keyword);
                // 2. 목표 페이지 이동
                FrameLocator searchFrame = page.frameLocator(SEARCH_IFRAME);
                hasNextPage = navigateToTargetPage(searchFrame, page, currentPage);
                if(!hasNextPage) continue;
                // 3. 식당 수집
                collectAllRestaurantsOnPage(page, searchFrame);
                // 4. 다음 페이지 존재 여부 확인 및 정보 업데이트
                hasNextPage = hasNextPageExists(searchFrame, currentPage);
                if(hasNextPage) currentPage++;
                else log.info("더 이상 이동할 페이지가 없습니다. 전체 수집 종료!");
            }
        }
    }

    private static void initSearchPage(Page page, String keyword){
        log.info("1. 접속 시도");
        String targetUrl = SEARCH_URL + keyword;
        page.navigate(targetUrl);

        try {
            log.info("지도가 완전히 로드될 때까지 대기 중...");
            page.waitForURL(url -> url.contains(LOAD_INDICATOR), new Page.WaitForURLOptions().setTimeout(15000));
        } catch (Exception e) {
            log.warn("좌표 로딩 대기 중 타임아웃이 발생했으나 계속 진행합니다.");
        }
    }

    private boolean navigateToTargetPage(FrameLocator searchFrame, Page page, int targetPage){
        if(targetPage <= 1){
            log.info("1페이지는 이동로직을 건너뜁니다.");
            return true;
        }

        log.info("{}페이지로 이동을 시도합니다.", targetPage);
        boolean pageFound = false;

        while(!pageFound){
            Locator nextNumBtn = searchFrame.locator(PAGE_NUMBER_SELECTOR)
                    .filter(new Locator.FilterOptions().setHasText(String.valueOf(targetPage)));

            if(nextNumBtn.isVisible()){
                nextNumBtn.click();
                page.waitForTimeout(3000); // 페이지 전환 대기
                pageFound=true;
                log.info("목표 페이지({}) 도달 성공", targetPage);
            }else{
                Locator nextArrowBtn = searchFrame.locator(NEXT_PAGE_ARROW_SELECTOR)
                        .filter(new Locator.FilterOptions().setHas(searchFrame.locator("span:has-text('" + NEXT_PAGE_TEXT + "')")));

                if(nextArrowBtn.isVisible() && !TRUE.equals(nextArrowBtn.getAttribute(ARIA_DISABLED))){
                    log.info("[다음] 화살표를 클릭하여 페이지 목록을 갱신합니다.");
                    nextArrowBtn.click();
                    page.waitForTimeout(3000);
                }else{
                    log.warn("목표 페이지를 찾지 못하고 마지막 페이지에 도달했습니다.");
                    return false;
                }
            }
        }
        return true;
    }

    private void collectAllRestaurantsOnPage(Page page, FrameLocator searchFrame){
        searchFrame.locator(RESTAURANT_ITEM_LINK).first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        int processedCount = 0;
        while(true){
            Locator restaurantLinks = searchFrame.locator(RESTAURANT_ITEM_LINK);
            int currentTotal = restaurantLinks.count();

            if (processedCount >= currentTotal) {
                // 마지막 요소로 스크롤 이동하여 로딩 트리거
                restaurantLinks.last().scrollIntoViewIfNeeded();
                page.waitForTimeout(1500); // 데이터 로드 대기
                // 스크롤 후에도 개수가 그대로라면 이 페이지는 진짜 끝! (혹은 다음 페이지 버튼 필요)
                if (searchFrame.locator(RESTAURANT_ITEM_LINK).count() == currentTotal) {
                    log.info("더 이상 추가되는 식당이 없습니다. 현재 페이지 수집 종료.");
                    break;
                }
                continue; // 개수가 늘어났으니 다시 위로 가서 수집 계속
            }
            processingSingleRestaurant(page, searchFrame, processedCount);
            processedCount++;

            applyRandomSleep(processedCount, page);
        }
    }

    private void processingSingleRestaurant(Page page, FrameLocator searchFrame, int index){
        try{
            log.info("--- {}번째 식당 작업 시작 ---", index + 1);
            // i번째 식당을 선택해서 클릭
            Locator target = searchFrame.locator(RESTAURANT_ITEM_LINK).nth(index);
            target.scrollIntoViewIfNeeded();
            target.click();

            // 식당 이름이 나타날 때까지 대기 (데이터 로딩 확인)
            FrameLocator detailFrame = page.frameLocator(ENTRY_IFRAME_SELECTOR);
            detailFrame.locator(TITLE_SELECTOR).waitFor(new Locator.WaitForOptions().setTimeout(5000));

            // 2. 데이터 수집 (파싱)
            String title = detailFrame.locator(TITLE_SELECTOR).innerText();
            String category = detailFrame.locator(CATEGORY_SELECTOR).innerText();
            String address = detailFrame.locator(ADDRESS_SELECTOR).innerText(); // LDgIH
            // 전화번호는 없을 수도 있으니 체크
            String phoneNumber = detailFrame.locator(PHONE_NUMBER_SELECTOR).isVisible() ?
                                detailFrame.locator(PHONE_NUMBER_SELECTOR).innerText() : NO_PHONE_NUMBER;
            log.info("결과: {} / {} / {} / {}", title, category, address, phoneNumber);

            // 카테고리 리스트 변환
            List<String> categoryList = Arrays.stream(category.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
            // 좌표 추출
            String[] coords = extractCoordinates(page);
            ensureOpeningHoursExpanded(detailFrame);

            List<RestaurantCrawledDto.OpeningHourDto> openingHourDtos = parseOpeningHours(detailFrame);

            RestaurantCrawledDto restaurantDto = RestaurantCrawledDto.builder()
                    .name(title)
                    .categories(categoryList)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .longitude(Double.parseDouble(coords[0]))
                    .latitude(Double.parseDouble(coords[1]))
                    .openingHours(openingHourDtos)
                    .build();
            naverCrawlerService.saveCrawledData(restaurantDto);
        }catch (Exception e){
            log.error("{}번째 식당 처리 중 에러 발생: {}", index + 1, e.getMessage());
        }
    }

    private static void ensureOpeningHoursExpanded(FrameLocator detailFrame) {
        // aria-expanded 상태를 체크하여 닫혀있을 때만 클릭
        Locator expandButton = detailFrame.locator(OPENING_HOUR_EXPAND_BTN_SELECTOR);
        if (FALSE.equals(expandButton.getAttribute(ARIA_EXPANDED))) {
            expandButton.click();
            log.info("영업시간 상세 보기 버튼을 클릭했습니다.");
        }
    }

    private static String[] extractCoordinates(Page page){
        Frame entryFrame = page.frames().stream()
                .filter(f -> ENTRY_IFRAME.equals(f.name()) || f.url().contains(ENTRY_IFRAME))
                .findFirst()
                .orElse(null);

        // 자바스크립트 실행하여 JSON 데이터 추출
        if (entryFrame != null) {
            String json = (String) entryFrame.evaluate("() => JSON.stringify(window.__APOLLO_STATE__)");

            if (json != null && !UNDEFINED.equals(json)) {
                return new String[]{extractCoordinate(json,X_COORD), extractCoordinate(json, Y_COORD)};
            }
        } else {
            log.error("상세페이지 프레임을 찾을 수 없습니다.");
        }
        return new String[]{DEFAULT_COORD, DEFAULT_COORD};
    }

    private boolean hasNextPageExists(FrameLocator searchFrame, int currentPage){
        Locator nextNumBtn = searchFrame.locator(PAGE_NUMBER_SELECTOR).filter(new Locator.FilterOptions().setHasText(String.valueOf(currentPage + 1)));
        Locator nextArrowBtn = searchFrame.locator(NEXT_PAGE_ARROW_SELECTOR).filter(new Locator.FilterOptions().setHas(searchFrame.locator("span:has-text('" + NEXT_PAGE_TEXT + "')")));
        return nextNumBtn.isVisible() || (nextArrowBtn.isVisible() && !TRUE.equals(nextArrowBtn.getAttribute(ARIA_DISABLED)));
    }

    private List<RestaurantCrawledDto.OpeningHourDto> parseOpeningHours(FrameLocator detailFrame){
        List<RestaurantCrawledDto.OpeningHourDto> openingHourDtos = new ArrayList<>();
        Locator days = detailFrame.locator(DAY_SELECTOR);
        Locator times = detailFrame.locator(TIMES_SELECTOR);

        int finalCount = Math.min(days.count(), times.count());
        log.info("발견된 영업시간 항목 수: {}", finalCount);

        for (int j = 0; j < finalCount; j++) {
            try{
                String dayText = days.nth(j).innerText(new Locator.InnerTextOptions().setTimeout(1000)).trim();
                String timeText = (String) times.nth(j).evaluate("el => el.innerText");

                if (timeText != null) {
                    openingHourDtos.add(createOpeningHourDto(timeText, dayText));
                }

            }catch (Exception e){
                log.warn("{}번째 영업시간 수집 실패", j);
            }
        }
        return openingHourDtos;
    }

    private static RestaurantCrawledDto.OpeningHourDto createOpeningHourDto(String timeText, String dayText) {
        String[] lines = timeText.split("\n");

        String openTime = null, closeTime = null, breakTime = null;
        StringBuilder lastOrders = new StringBuilder();

        for (String line : lines) {
            Matcher matcher = TIME_PATTERN.matcher(line);
            List<String> timesInLine = new ArrayList<>();

            while (matcher.find()) timesInLine.add(matcher.group());

            if(timesInLine.isEmpty()) continue;

            if (line.contains(BREAK_TIME_LABEL)) {
                breakTime = String.join(" - ", timesInLine);
            } else if (line.contains(LAST_ORDER_LABEL)) {
                // "라스트오더: 21:30" 형태를 유지하며 추가
                if (lastOrders.length() > 0) lastOrders.append(" | ");
                lastOrders.append(String.join(", ", timesInLine));
            } else if (openTime == null && timesInLine.size() >= 2) {
                // 보통 첫 번째로 나오는 시간 쌍이 영업시간
                openTime = timesInLine.get(0);
                closeTime = timesInLine.get(1);
            }
        }

        log.info("정제데이터 -> 요일:{}, 시작: {}, 종료: {}, 브레이크: {}, 라스트오더: {}",
                dayText, openTime, closeTime, breakTime, lastOrders);

        return RestaurantCrawledDto.OpeningHourDto.builder()
                .dayOfWeek(dayText)
                .startTime(openTime)
                .endTime(closeTime)
                .breakTime(breakTime)
                .lastOrder(lastOrders.toString())
                .build();
    }

    /**
     * JSON 문자열에서 정규표현식을 이용해 좌표값을 추출합니다.
     */
    private static String extractCoordinate(String json, Pattern pattern) {
        if (json == null) return DEFAULT_COORD;
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return DEFAULT_COORD;
    }

    private static BrowserContext createNewContext(Browser browser) {
        return browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(USER_AGENT));
    }

    private static void applyRandomSleep(int processedCount, Page page) {
        long randomDelay = java.util.concurrent.ThreadLocalRandom.current().nextLong(4000, 6000);
        log.info("--- {}번째 식당 저장 완료. 사람인 척 {}초 대기 중... ---", processedCount, randomDelay / 1000.0);
        page.waitForTimeout(randomDelay);

        // 10개 단위로 잠시 휴식 (차단 방지)
        if (processedCount % 10 == 0) {
            log.info("10개 수집 완료. 3초간 휴식합니다.");
            page.waitForTimeout(3000);
        }
    }
}