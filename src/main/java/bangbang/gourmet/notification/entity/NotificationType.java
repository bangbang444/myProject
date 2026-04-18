package bangbang.gourmet.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    LIKE("새 좋아요", "%s님이 회원님의 리뷰를 좋아합니다.", "/review/%d"),
    COMMENT("새 댓글", "%s님이 회원님의 리뷰에 댓글을 남겼습니다.", "/review/%d"),
    FOLLOW("새 팔로워", "%s님이 회원님을 팔로우하기 시작했습니다.", "/user/%d");

    private final String title;
    private final String bodyTemplate;
    private final String urlTemplate;

    public String getBody(String senderNickname) {
        return String.format(bodyTemplate, senderNickname);
    }

    public String getUrl(Long targetId) {
        if (targetId == null) return "";
        return String.format(urlTemplate, targetId);
    }
}
