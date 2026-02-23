package bangbang.gourmet.user.controller.dto;

public record ProfileImageUpdateDto(
        String profileImageKey
) {
    public static ProfileImageUpdateDto from(String profileImageKey) {
        return new ProfileImageUpdateDto(profileImageKey);
    }
}
