package backend.example.mxh.repository;

import backend.example.mxh.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversation_Id(Long conversationId, Pageable pageable);


    @Query(value = """
            select * from messages as m
            where m.conversation_id = :conversationId
            order by m.created_at desc 
            limit 1
            """, nativeQuery = true)
    Message findTopMessage(@Param("conversationId") Long conversationId);

    Page<Message> findByConversation_IdAndCreatedAtAfter(Long conversation_id, LocalDateTime createdAt, Pageable pageable);
}

