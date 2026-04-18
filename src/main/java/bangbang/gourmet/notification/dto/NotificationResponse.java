package bangbang.gourmet.notification.dto;

import bangbang.gourmet.notification.entity.Notification;
import bangbang.gourmet.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String senderNickname,
        String senderProfileImageKey,
        Long targetId,
        boolean isRead,
        LocalDateTime createdDate
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getSender().getNickname(),
                notification.getSender().getProfileImageKey(),
                notification.getTargetId(),
                notification.isRead(),
                notification.getCreatedDate()
        );
    }
}
