package bangbang.gourmet.notification.repository;

import bangbang.gourmet.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.sender WHERE n.receiver.id = :receiverId AND (:cursorId IS NULL OR n.id < :cursorId) ORDER BY n.id DESC")
    List<Notification> findByReceiverIdWithCursor(@Param("receiverId") Long receiverId, @Param("cursorId") Long cursorId, Pageable pageable);

    long countByReceiverIdAndIsRead(Long receiverId, boolean isRead);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}
