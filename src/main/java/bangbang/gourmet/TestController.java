package bangbang.gourmet;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.crawler.NaverCrawlSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final NaverCrawlSyncService naverCrawlSyncService;

    @GetMapping("/success")
    public Response<String> success(){
        return Response.success(SuccessCode.SUCCESS, "success");
    }

    @GetMapping("/error-test")
    public Response<String> error(){
        throw new RuntimeException("디스코드 알림 테스트용 에러 발생");
        //return Response.error(ErrorCode.INTERNAL_SERVER_ERROR, "error");
    }

    @GetMapping("/crawl")
    public Response<String> crawl(){
        naverCrawlSyncService.startAsyncCrawl();
        return Response.success(SuccessCode.SUCCESS, "crawl");
    }

    @GetMapping("/api/test")
    public void test(@UserId Long id){
        log.info("id:{}", id);
    }
}
