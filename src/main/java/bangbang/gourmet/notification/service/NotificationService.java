package bangbang.gourmet.notification.service;

import bangbang.gourmet.notification.entity.Notification;
import bangbang.gourmet.notification.entity.NotificationType;
import bangbang.gourmet.notification.repository.NotificationRepository;
import bangbang.gourmet.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    @Transactional
    public void notify(User receiver, User sender, NotificationType type, Long targetId) {
        if (receiver.getId().equals(sender.getId())) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .targetId(targetId)
                .build());

        fcmService.sendNotification(receiver.getFcmToken(), type, sender.getNickname());
    }
}
