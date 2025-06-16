package backend.example.mxh.repository;

import backend.example.mxh.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversation_IdAndMember_IdAndAdmin(Long conversationId, Long memberId, boolean admin);

    void deleteByConversation_IdAndMember_Id(Long conversationId, Long memberId);

    Optional<ConversationMember> findByConversation_IdAndMember_Id(Long conversationId, Long memberId);

    long countByConversation_Id(Long conversationId);

    List<ConversationMember> findByConversation_Id(Long conversationId);

    long countByConversation_IdAndAdmin(Long conversationId, boolean admin);
}