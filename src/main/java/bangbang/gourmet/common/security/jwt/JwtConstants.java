package bangbang.gourmet.common.security.jwt;

public class JwtConstants {
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L; // 30분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60 * 1000L; // 14일
    public static final long REFRESH_TOKEN_EXPIRE_TIME_SECONDS = REFRESH_TOKEN_EXPIRE_TIME / 1000L; // 14일

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // 인스턴스화 방지
    private JwtConstants(){}
}
