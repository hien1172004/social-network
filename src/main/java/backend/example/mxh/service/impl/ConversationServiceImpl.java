package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.AddMemberDTO;
import backend.example.mxh.DTO.request.ConversationDTO;
import backend.example.mxh.DTO.request.ConversationMemberDTO;
import backend.example.mxh.DTO.request.RemoveMemberDTO;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.entity.Conversation;
import backend.example.mxh.entity.ConversationMember;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.ConversationMapper;
import backend.example.mxh.repository.ConversationMemberRepository;
import backend.example.mxh.repository.ConversationRepository;
import backend.example.mxh.repository.MessageRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.ConversationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(ConversationDTO conversationDTO) {
        // Validate dữ liệu đầu vào
        if (conversationDTO.getMemberIds() == null || conversationDTO.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("Conversation must have at least one member");
        }
        User creator = userRepository.findById(conversationDTO.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));
        List<User> members = userRepository.findAllById(conversationDTO.getMemberIds());
        Conversation conversation = conversationMapper.toConversation(conversationDTO);
        conversation.setGroup(members.size() > 1);
        List<ConversationMember> memberEntities = new ArrayList<>();
        for (User member : members) {
            memberEntities.add(ConversationMember.builder()
                    .conversation(conversation)
                    .member(member)
                    .admin(false)
                    .build());
        }
        if(members.size() > 1) {
            memberEntities.add(ConversationMember.builder()
                    .conversation(conversation)
                    .member(creator)
                    .admin(true)
                    .build());
        }
        else{
            memberEntities.add(ConversationMember.builder()
                    .conversation(conversation)
                    .member(creator)
                    .admin(false)
                    .build());
        }
        conversation.setMembers(memberEntities);
        conversation = conversationRepository.save(conversation);
        return conversationMapper.toResponse(conversation);
    }

    @Override
    public ConversationResponse getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return conversationMapper.toResponse(conversation);
    }

    @Override
    public List<ConversationResponse> getConversationsByUserId(Long userId) {
        return List.of();
    }

    @Override
    @Transactional
    public void addMemberToConversation(AddMemberDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if (!isAdmin) throw new InvalidDataException("Only admin can add members");
        List<User> users = userRepository
                .findAllById(dto.getMemberIds());
        for (User user : users) {
            ConversationMember member = ConversationMember.builder()
                    .conversation(conversation)
                    .member(user)
                    .admin(false)
                    .build();
            conversationMemberRepository.save(member);
        };
        log.info("Add member to conversation");
    }

    @Override
    public void removeMemberFromConversation(RemoveMemberDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if (!isAdmin) throw new InvalidDataException("Only admin can add members");

        conversationMemberRepository.deleteByConversation_IdAndMember_Id(conversation.getId(), dto.getMemberId());
        log.info("Remove member: " + dto.getMemberId());
    }

    @Override
    public void updateConversation(Long conversationId, ConversationDTO conversationDTO) {

    }

    @Override
    public void updateConversationAvatar(Long conversationId, String avatarUrl) {

    }

    @Override
    public void updateMemberRole(Long conversationId, Long userId, String role) {

    }

    @Override
    public void leaveConversation(Long conversationId, Long userId) {

    }

    @Override
    public void deleteConversation(Long conversationId) {

    }

    @Override
    public List<ConversationMemberDTO> getConversationMembers(Long conversationId) {
        return List.of();
    }

    @Override
    public boolean isAdmin(Long conversationId, Long userId) {
        return false;
    }
}
