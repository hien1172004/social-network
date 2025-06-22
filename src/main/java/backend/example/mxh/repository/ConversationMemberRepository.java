package backend.example.mxh.repository;

import backend.example.mxh.entity.ConversationMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversation_IdAndMember_IdAndAdmin(Long conversationId, Long memberId, boolean admin);

    @Modifying
    @Query(value = """
delete from conversation_members cm where cm.conversation_id = ?1 and cm.user_id = ?2
""", nativeQuery = true)
    void deleteByConversation_IdAndMember_Id(Long conversationId, Long memberId);

    Optional<ConversationMember> findByConversation_IdAndMember_Id(Long conversationId, Long memberId);

    long countByConversation_Id(Long conversationId);

    List<ConversationMember> findByConversation_Id(Long conversationId);

    long countByConversation_IdAndAdmin(Long conversationId, boolean admin);

    @Query("""
select cm from ConversationMember cm
where cm.conversation.id = :conversationId
and cm.member.username like concat('%', :key, '%')
""")
    Page<ConversationMember> findMemberInConversation(@Param("conversationId") Long conversationId,@Param("key") String key, Pageable pageable);

    Page<ConversationMember> findByConversation_Id(Long conversationId, Pageable pageable);

    boolean existsByConversation_IdAndMember_Id(Long conversationId, Long memberId);
}