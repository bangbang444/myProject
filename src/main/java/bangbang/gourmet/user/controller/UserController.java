package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserAuthService userService;

    // TODO: 프론트엔드 생기면 바꿀 예정
    @GetMapping("/auth/kakao/callback")
    public Response<LoginResponse> kakaoLogin(@RequestParam String code){
        return Response.success(SuccessCode.SUCCESS, userService.loginWithKakao(code));
    }
}
