package bangbang.gourmet.user.controller.dto;

import bangbang.gourmet.user.entity.User;

public record ProfileResponseDto(
     Long id,
     String nickname, // 닉네임
     String bio, // 상태 메시지
     String profileImageKey,
     long followerCount,
     long followingCount,
     long postCount
) {
    public static ProfileResponseDto from(User user, String profileImageKey, long followerCount, long followingCount, long postCount) {
        return new ProfileResponseDto(
                user.getId(),
                user.getNickname(),
                user.getBio(),
                profileImageKey,
                followerCount,
                followingCount,
                postCount
        );
    }
}
