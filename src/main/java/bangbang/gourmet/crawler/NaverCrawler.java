package bangbang.gourmet.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NaverCrawler {
    public void crawl() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setSlowMo(150));

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"));

            Page page = context.newPage();

            // 1. 검색 페이지 접속
            page.navigate("https://map.naver.com/p/search/강남역 맛집");
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

            int currentPage = 1;
            boolean hasNextPage = true;

            while(hasNextPage) {
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
                        String address = detailFrame.locator("span.LDgIH").innerText();

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

                        int count = days.count();
                        for (int j = 0; j < count; j++) {
                            log.info("요일: {}, 시간: {}", days.nth(j).innerText(), times.nth(j).innerText());
                        }

                        processedCount++;

                        // [병현 님 아이디어] 10개 단위로 잠시 휴식 (차단 방지)
                        if (processedCount % 10 == 0) {
                            log.info("10개 수집 완료. 2초간 휴식합니다.");
                            page.waitForTimeout(2000);
                        }

                    } catch (Exception e) {
                        log.error("{}번째 식당 처리 중 에러 발생: {}", processedCount + 1, e.getMessage());
                        // 에러가 나도 다음 식당으로 넘어가도록 continue 역할
                    }
                } // 한 페이지 순회 완료 (while)

                // 다음 숫자 버튼(currentPage + 1)을 먼저 찾고, 없으면 [다음] 화살표를 찾습니다.
                Locator nextNumBtn = searchFrame.locator("a.mBN2s").filter(new Locator.FilterOptions().setHasText(String.valueOf(currentPage + 1)));
                Locator nextArrowBtn = searchFrame.locator("a.eUTV2").filter(new Locator.FilterOptions().setHas(searchFrame.locator("span:has-text('다음페이지')")));

                if (nextNumBtn.isVisible()) {
                    log.info("다음 숫자 페이지({})로 이동합니다.", currentPage + 1);
                    nextNumBtn.click();
                    currentPage++;
                    page.waitForTimeout(3000); // 페이지 전환 대기
                } else if (nextArrowBtn.isVisible() && !"true".equals(nextArrowBtn.getAttribute("aria-disabled"))) {
                    log.info("다음 페이지 묶음(>)으로 이동합니다.");
                    nextArrowBtn.click();
                    currentPage++;
                    page.waitForTimeout(3000);
                } else {
                    log.info("더 이상 이동할 페이지가 없습니다. 전체 수집 종료!");
                    hasNextPage = false; // 바깥쪽 while 루프 탈출
                }
            }

            browser.close();
        } catch (Exception e) {
            log.error("크롤링 중 에러 발생: ", e);
        }
    }
}