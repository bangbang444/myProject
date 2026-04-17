package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.common.security.jwt.JwtConstants;
import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import static bangbang.gourmet.common.security.jwt.JwtConstants.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserAuthService userService;

    @PostMapping("/auth/login")
    public Response<LoginResponse> kakaoLogin(
            @RequestBody LoginRequest request,
            HttpServletResponse httpServletResponse
    ){
        LoginResponse loginResponse = userService.loginWithKakao(request);

        // Refresh Token을 HttpOnly Cookie로 설정 (웹 브라우저용)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)           // JavaScript로 접근 불가 (XSS 방지)
                .secure(true)             // HTTPS에서만 전송,
                .sameSite("Strict")       // CSRF 방지
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME_SECONDS)  // 60일 (초 단위)
                .path("/")                // 모든 경로에서 전송
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("Refresh token cookie 설정 완료");

        // Response Body에도 refreshToken 포함 (모바일 앱용)
        return Response.success(SuccessCode.SUCCESS, loginResponse);
    }

    @PostMapping("/auth/logout")
    public Response<Void> logout(
            @UserId Long userId,
            HttpServletResponse httpServletResponse
    ) {
        userService.logout(userId);

        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return Response.success(SuccessCode.SUCCESS, null);
    }
}
