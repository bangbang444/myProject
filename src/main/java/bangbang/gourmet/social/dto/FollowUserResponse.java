package bangbang.gourmet.social.dto;

import bangbang.gourmet.user.entity.User;

public record FollowUserResponse(
        Long userId,
        String nickname,
        String profileImageKey,
        boolean isFollowing
) {
    public static FollowUserResponse of(User user, boolean isFollowing) {
        return new FollowUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageKey(),
                isFollowing
        );
    }
}
