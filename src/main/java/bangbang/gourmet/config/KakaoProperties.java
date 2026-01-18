package bangbang.gourmet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.client")
public record KakaoProperties(
        String id,
        String secret,
        String redirectUri
) {
}
