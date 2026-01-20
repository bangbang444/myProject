package bangbang.gourmet.common.security.jwt.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {}
