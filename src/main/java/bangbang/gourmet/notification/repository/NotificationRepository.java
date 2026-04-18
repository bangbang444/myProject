package bangbang.gourmet.notification.repository;

import bangbang.gourmet.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
