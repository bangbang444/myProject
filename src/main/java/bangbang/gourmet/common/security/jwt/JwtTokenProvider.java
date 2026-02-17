package bangbang.gourmet.common.security.jwt;

import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.common.exception.model.UnauthorizedException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.security.jwt.dto.TokenPair;
import bangbang.gourmet.common.security.jwt.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static bangbang.gourmet.common.security.jwt.JwtConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private Key signingKey;

    @PostConstruct
    protected void init() {
        final byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenPair generateTokenPair(Long userId, SocialProvider provider) {
        // 기존 토큰 삭제 (재로그인 시)
        refreshTokenService.deleteRefreshToken(userId);

        String accessToken = createAccessToken(userId, provider);
        String refreshToken = createRefreshToken(userId, provider);

        // Redis에 리프레시 토큰 저장
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        return new TokenPair(accessToken, refreshToken);
    }

    private String createAccessToken(Long userId, SocialProvider provider) {
        Claims claims = getAccessTokenClaims(userId, provider);
        return createToken(claims);
    }

    private String createRefreshToken(Long userId, SocialProvider provider) {
        Claims claims = getRefreshTokenClaims(userId, provider);
        return createToken(claims);
    }

    private Claims getAccessTokenClaims(Long userId, SocialProvider provider){
        Date now = new Date();
        Claims claims = Jwts.claims()
                .setSubject(String.valueOf(userId));

        claims.put("provider", provider.name());
        claims.put("type", "ACCESS_TOKEN");

        return claims.setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME));
    }

    private Claims getRefreshTokenClaims(Long userId, SocialProvider provider) {
        Date now = new Date();
        Claims claims = Jwts.claims()
                .setSubject(String.valueOf(userId));

        claims.put("provider", provider.name());
        claims.put("type", "REFRESH_TOKEN");

        return claims.setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME));
    }

    private String createToken(Claims claims) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(this.signingKey)
                .compact(); // 자동 인코딩
    }

    public boolean validateToken(String token) {
        try{
            Claims claims = getBody(token);

            if(!"ACCESS_TOKEN".equals(claims.get("type"))){
                throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_USER);
            }

            return !claims.getExpiration().before(new Date());
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public String getSubject(String token) {
        return getBody(token).getSubject();
    }

    private Claims getBody(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
