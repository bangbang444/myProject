package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    public Response<LoginResponse> kakaoLogin(
            @RequestParam String code,
            HttpServletResponse httpServletResponse
    ){
        LoginResponse loginResponse = userService.loginWithKakao(code);

        // Refresh Token을 HttpOnly Cookie로 설정 (웹 브라우저용)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)           // JavaScript로 접근 불가 (XSS 방지)
                .secure(true)             // HTTPS에서만 전송,
                .sameSite("Strict")       // CSRF 방지
                .maxAge(60 * 24 * 60 * 60)  // 60일 (초 단위)
                .path("/")                // 모든 경로에서 전송
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("Refresh token cookie 설정 완료");

        // Response Body에도 refreshToken 포함 (모바일 앱용)
        return Response.success(SuccessCode.SUCCESS, loginResponse);
    }
}
