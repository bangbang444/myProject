package bangbang.gourmet.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.jwt")
public record JwtProperties(
    String secret
) {}
