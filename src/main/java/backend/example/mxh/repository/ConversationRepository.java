package backend.example.mxh.repository;

import backend.example.mxh.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
             select c from Conversation c
             LEFT JOIN c.members cm
             LEFT JOIN c.messages m
             WHERE cm.member.id = :userId
             group by c.id
             order by coalesce(max(m.createdAt),c.createdAt) desc,
                         (select count(ms) from MessageStatus ms where ms.message.conversation.id = c.id and ms.user.id=:userId and ms.isRead = false ) DESC 
            """)
    Page<Conversation> findAllConversationsByUser_Id(@RequestParam("userId") Long userId, Pageable pageable);
}