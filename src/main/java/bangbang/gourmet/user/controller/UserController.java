package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.KakaoTokenResponse;
import bangbang.gourmet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // TODO: 프론트엔드 생기면 바꿀 예정
    @GetMapping("/auth/kakao/callback")
    public Response<String> signup(@RequestParam String code){
        KakaoTokenResponse tokens = userService.getKakaoToken(code);

        return Response.success(SuccessCode.SUCCESS, "호출 성공");
    }
}
