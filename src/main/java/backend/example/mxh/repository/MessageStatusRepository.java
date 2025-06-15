package backend.example.mxh.repository;

import backend.example.mxh.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus> findByMessage_IdAndUser_Id(Long messageId, Long userId);

    @Query("""
        SELECT ms FROM MessageStatus ms
        JOIN FETCH ms.message m
        JOIN FETCH m.conversation c
        WHERE c.id = :conversationId
          AND ms.user.id = :userId
          AND ms.isRead = false
    """)
    List<MessageStatus> getMessageNotRead(@Param("conversationId") Long conversationId,
                                          @Param("userId") Long userId);


    @Query("""
        SELECT COUNT(ms.id) FROM MessageStatus ms
        JOIN ms.message m
        JOIN m.conversation c
        WHERE c.id = :conversationId
          AND ms.user.id = :userId
          AND ms.isRead = false
    """)
    Long countMessageNotRead(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId);
}
