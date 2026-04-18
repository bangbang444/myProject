package bangbang.gourmet.notification.service;

import bangbang.gourmet.notification.dto.NotificationResponse;
import bangbang.gourmet.notification.entity.Notification;
import bangbang.gourmet.notification.repository.NotificationRepository;
import bangbang.gourmet.review.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public CursorPageResponse<NotificationResponse> getNotifications(Long userId, Long cursorId, int size) {
        if (cursorId == null) {
            notificationRepository.markAllAsReadByReceiverId(userId);
        }

        List<Notification> notifications = notificationRepository.findByReceiverIdWithCursor(
                userId, cursorId, PageRequest.of(0, size + 1));

        boolean hasNext = notifications.size() > size;
        List<Notification> page = hasNext ? notifications.subList(0, size) : notifications;

        List<NotificationResponse> items = page.stream()
                .map(NotificationResponse::from)
                .toList();

        Long nextCursorId = (hasNext && !page.isEmpty()) ? page.get(page.size() - 1).getId() : null;
        return CursorPageResponse.of(items, nextCursorId, hasNext);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsRead(userId, false);
    }

}
