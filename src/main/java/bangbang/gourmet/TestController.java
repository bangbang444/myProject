package bangbang.gourmet;

import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/success")
    public Response<String> success(){
        return Response.success(SuccessCode.SUCCESS, "success");
    }

    @GetMapping("/error")
    public Response<String> error(){
        return Response.error(ErrorCode.INTERNAL_SERVER_ERROR, "error");
    }
}
