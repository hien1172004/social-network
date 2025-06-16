package backend.example.mxh.repository;

import backend.example.mxh.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            select c from Conversation c
            join fetch ConversationMember cm on c.id = cm.conversation.id
            where cm.member.id = ?1
            """)
    List<Conversation> findAllConversationsByUser_Id(Long userId);
}