package bangbang.gourmet.crawler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaverCrawlSyncServiceTest {

    @InjectMocks
    private NaverCrawlSyncService naverCrawlSyncService;

    @Mock private NaverCrawler naverCrawler;
    @Mock private CrawlTargetManger crawlTargetManger;

    @Test
    @DisplayName("크롤링 중이 아니면 crawlAll()이 호출되어야 한다")
    void startAsyncCrawl_WhenNotRunning_ShouldStartCrawl() {
        // given
        given(crawlTargetManger.getDefaultKeywords()).willReturn(List.of("강남 맛집"));

        // when
        naverCrawlSyncService.startAsyncCrawl();

        // then
        verify(naverCrawler).crawlAll(any());
    }

    @Test
    @DisplayName("크롤링 중이면 crawlAll()이 호출되지 않아야 한다")
    void startAsyncCrawl_WhenAlreadyRunning_ShouldSkip() {
        // given
        ReflectionTestUtils.setField(naverCrawlSyncService, "isCrawling", new AtomicBoolean(true));

        // when
        naverCrawlSyncService.startAsyncCrawl();

        // then
        verify(naverCrawler, never()).crawlAll(any());
    }

    @Test
    @DisplayName("crawlAll() 예외 발생 시에도 플래그가 해제되어야 한다")
    void startAsyncCrawl_WhenExceptionOccurs_ShouldReleaseLock() {
        // given
        given(crawlTargetManger.getDefaultKeywords()).willReturn(List.of("강남 맛집"));
        doThrow(new RuntimeException("크롤링 실패")).doNothing().when(naverCrawler).crawlAll(any());

        // when
        try {
            naverCrawlSyncService.startAsyncCrawl();
        } catch (Exception ignored) {}

        // then - 플래그가 해제되어 다음 호출은 정상 실행되어야 함
        given(crawlTargetManger.getDefaultKeywords()).willReturn(List.of("강남 맛집"));
        naverCrawlSyncService.startAsyncCrawl();
        verify(naverCrawler, times(2)).crawlAll(any());
    }
}