package backend.example.mxh.repository;

import backend.example.mxh.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiver_Id(Long receiverId, Pageable pageable);
    @Query("""
    SELECT n FROM Notification n
    WHERE n.receiver.id = :userId AND n.isRead = false
""")
    Page<Notification> findByReceiver_IdAndIsRead(@Param("userId") long userId, Pageable pageable);

    @Query("""
    SELECT n FROM Notification n
    WHERE n.receiver.id = :receiverId AND n.isRead = false
""")
    List<Notification> findByReceiver_IdAndRead(Long receiverId);

    @Query("""
    select count(*) from Notification n
    where n.receiver.id = :receiverId AND n.isRead = false
""")
    long countByReceiverIdAndReadIsFalse(Long receiverId);
}