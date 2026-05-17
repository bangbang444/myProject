package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.service.SocialAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static bangbang.gourmet.common.util.CookieUtils.createExpiredRefreshTokenCookie;
import static bangbang.gourmet.common.util.CookieUtils.createRefreshTokenCookie;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final SocialAuthService userAuthService;

    @PostMapping("/auth/login")
    public Response<LoginResponse> kakaoLogin(
            @RequestBody LoginRequest request,
            HttpServletResponse httpServletResponse
    ){
        LoginResponse loginResponse = userAuthService.login(request);
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