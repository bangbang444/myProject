package bangbang.gourmet.notification.service;

import bangbang.gourmet.notification.entity.NotificationType;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    @Async("notificationExecutor")
    public void sendNotification(String fcmToken, NotificationType type, String senderNickname, String url) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .putData("title", type.getTitle())
                .putData("body", type.getBody(senderNickname))
                .putData("url", url != null ? url : "")
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - token: {}, type: {}", fcmToken, type, e);
        }
    }
}
