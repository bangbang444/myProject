package bangbang.gourmet.common.security.jwt;

import bangbang.gourmet.common.security.jwt.dto.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private Key signingKey;
    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final long DAYS_IN_MILLISECONDS = 24 * 60 * 60 * 1000L;
    private static final int ACCESS_TOKEN_EXPIRATION_DAYS = 30;
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 60;

    @PostConstruct
    protected void init() {
        final byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPair generateTokenPair(String email) {
        // TODO: 레디스에 리프레시 토큰 제거(id)
        String accessToken = createAccessToken(email);
        String refreshToken = createRefreshToken(email);
        // TODO: 레디스에 리프레시 토큰 저장(id, refreshToken)
        return new TokenPair(accessToken, refreshToken);
    }

    private String createAccessToken(String email) {
        Claims claims = getAccessTokenClaims();
        claims.put(USER_EMAIL, email);
        return createToken(claims);
    }

    private String createRefreshToken(String email) {
        Claims claims = getRefreshTokenClaims();
        claims.put(USER_EMAIL, email);
        return createToken(claims);
    }

    private Claims getAccessTokenClaims(){
        Date now = new Date();
        return Jwts.claims()
                .setSubject(ACCESS_TOKEN)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_DAYS *  DAYS_IN_MILLISECONDS));
    }

    private Claims getRefreshTokenClaims() {
        Date now = new Date();
        return Jwts.claims()
                .setSubject(REFRESH_TOKEN)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_DAYS *  DAYS_IN_MILLISECONDS));
    }

    private String createToken(Claims claims) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(this.signingKey)
                .compact(); // 자동 인코딩
    }
}
