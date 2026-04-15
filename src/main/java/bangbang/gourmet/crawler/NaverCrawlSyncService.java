package bangbang.gourmet.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverCrawlSyncService {

    private final NaverCrawler naverCrawler;
    private final CrawlTargetManager crawlTargetManager;

    private final AtomicBoolean isCrawling = new AtomicBoolean(false);

    @Async("crawlTaskExecutor") // 별도의 스레드에서 실행되어 컨트롤러 응답을 방해하지 않음
    public void startAsyncCrawl() {
        if (!isCrawling.compareAndSet(false, true)) {
            log.warn(">>>> 크롤링이 이미 실행 중입니다. 요청을 무시합니다. <<<<");
            return;
        }
        try {
            log.info(">>>> [비동기] 네이버 크롤링 동기화 작업을 시작합니다. <<<<");
            List<String> keywords = crawlTargetManager.getDefaultKeywords();
            naverCrawler.crawlAll(keywords);
            log.info(">>>> [비동기] 모든 지역 크롤링 완료 <<<<");
        } finally {
            isCrawling.set(false);
        }
    }

}
