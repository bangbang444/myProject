package bangbang.gourmet.common.util;

import org.springframework.http.ResponseCookie;

import static bangbang.gourmet.common.security.jwt.JwtConstants.*;

public class CookieUtils {

    private CookieUtils() {}

    public static ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME_SECONDS)
                .path("/")
                .build();
    }

    public static ResponseCookie createExpiredRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .path("/")
                .build();
    }
}