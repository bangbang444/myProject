package bangbang.gourmet.user.controller.dto;

import bangbang.gourmet.user.entity.User;

public record ProfileResponseDto(
     Long id,
     String nickname, // 닉네임
     String bio, // 상태 메시지
     String profileImageKey,
     long followerCount,
     long followingCount,
     long postCount,
     boolean isFollowing
) {
    public static ProfileResponseDto from(User user, long followerCount, long followingCount, long postCount) {
        return new ProfileResponseDto(
                user.getId(),
                user.getNickname(),
                user.getBio(),
                user.getProfileImageKey(),
                followerCount,
                followingCount,
                postCount,
                false
        );
    }

    public static ProfileResponseDto from(User user, long followerCount, long followingCount, long postCount, boolean isFollowing) {
        return new ProfileResponseDto(
                user.getId(),
                user.getNickname(),
                user.getBio(),
                user.getProfileImageKey(),
                followerCount,
                followingCount,
                postCount,
                isFollowing
        );
    }
}
