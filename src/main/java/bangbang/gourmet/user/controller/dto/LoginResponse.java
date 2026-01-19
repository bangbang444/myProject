package bangbang.gourmet.user.controller.dto;

public record LoginResponse(
        String accessToken
) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token);
    }
}
