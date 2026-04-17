package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.common.util.CookieUtils;
import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import static bangbang.gourmet.common.util.CookieUtils.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserAuthService userAuthService;

    @PostMapping("/auth/login")
    public Response<LoginResponse> kakaoLogin(
            @RequestBody LoginRequest request,
            HttpServletResponse httpServletResponse
    ){
        LoginResponse loginResponse = userAuthService.loginWithKakao(request);
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(loginResponse.refreshToken()).toString());
        log.info("Refresh token cookie 설정 완료");
        return Response.success(SuccessCode.SUCCESS, loginResponse);
    }

    @PostMapping("/auth/logout")
    public Response<Void> logout(
            @UserId Long userId,
            HttpServletResponse httpServletResponse
    ) {
        userAuthService.logout(userId);
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, createExpiredRefreshTokenCookie().toString());
        return Response.success(SuccessCode.SUCCESS, null);
    }
}