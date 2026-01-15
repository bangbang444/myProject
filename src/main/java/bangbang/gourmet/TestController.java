package bangbang.gourmet;

import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.crawler.NaverCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final NaverCrawler naverCrawler;

    @GetMapping("/success")
    public Response<String> success(){
        return Response.success(SuccessCode.SUCCESS, "success");
    }

    @GetMapping("/error")
    public Response<String> error(){
        return Response.error(ErrorCode.INTERNAL_SERVER_ERROR, "error");
    }

    @GetMapping("/crawl")
    public Response<String> crawl(){
        naverCrawler.crawl();
        return Response.success(SuccessCode.SUCCESS, "crawl");
    }
}
