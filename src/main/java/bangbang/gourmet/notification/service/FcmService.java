package bangbang.gourmet.notification.service;

import bangbang.gourmet.notification.entity.NotificationType;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendNotification(String fcmToken, NotificationType type, String senderNickname) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(type.getTitle())
                        .setBody(type.getBody(senderNickname))
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - token: {}, type: {}", fcmToken, type, e);
        }
    }
}
