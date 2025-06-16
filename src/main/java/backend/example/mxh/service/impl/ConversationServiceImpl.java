package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.*;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.entity.Conversation;
import backend.example.mxh.entity.ConversationMember;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.ConversationMapper;
import backend.example.mxh.mapper.ConversationMemberMapper;
import backend.example.mxh.repository.ConversationMemberRepository;
import backend.example.mxh.repository.ConversationRepository;
import backend.example.mxh.repository.MessageRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.ConversationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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
    private final ConversationMemberMapper conversationMemberMapper;
    @Override
    @Transactional
    public ConversationResponse createConversation(ConversationDTO conversationDTO) {
        // Validate dữ liệu đầu vào
        if (conversationDTO.getMemberIds() == null || conversationDTO.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("Cuộc trò chuyện phải có ít nhất một thành viên");
        }

        // Giới hạn số lượng thành viên tối đa
        final int MAX_MEMBERS = 100;
        if (conversationDTO.getMemberIds().size() > MAX_MEMBERS) {
            throw new IllegalArgumentException("Số lượng thành viên vượt quá giới hạn: " + MAX_MEMBERS);
        }

        // Tìm người tạo
        User creator = userRepository.findById(conversationDTO.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người tạo với ID: " + conversationDTO.getCreatorId()));

        // Tìm các thành viên và kiểm tra tính hợp lệ
        List<User> members = userRepository.findAllById(conversationDTO.getMemberIds());
        if (members.size() != conversationDTO.getMemberIds().size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều thành viên không tồn tại");
        }

        // Kiểm tra xem creator có trong memberIds không
        boolean creatorInMembers = conversationDTO.getMemberIds().contains(conversationDTO.getCreatorId());

        // Chuyển đổi DTO thành entity
        Conversation conversation = conversationMapper.toConversation(conversationDTO);
        conversation.setGroup(members.size() > 1 || creatorInMembers);

        // Tạo danh sách thành viên
        List<ConversationMember> memberEntities = new ArrayList<>();

        // Thêm các thành viên (không bao gồm creator nếu đã có trong memberIds)
        for (User member : members) {
            if (!member.getId().equals(conversationDTO.getCreatorId())) {
                memberEntities.add(ConversationMember.builder()
                        .conversation(conversation)
                        .member(member)
                        .admin(!conversation.isGroup()) // Trong nhóm, chỉ creator là admin
                        .build());
            }
        }

        // Thêm creator vào danh sách thành viên (luôn là admin)
        memberEntities.add(ConversationMember.builder()
                .conversation(conversation)
                .member(creator)
                .admin(true)
                .build());

        // Gán thành viên và lưu
        conversation.setMembers(memberEntities);
        conversation = conversationRepository.save(conversation);

        // Trả về response
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
        List<Conversation> conversations = conversationRepository.findAllConversationsByUser_Id(userId);
        return conversations.stream().map(conversationMapper::toResponse).toList();
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
        }
        log.info("Add member to conversation");
    }

    @Override
    @Transactional
    public void removeMemberFromConversation(RemoveMemberDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if (!isAdmin) throw new InvalidDataException("Only admin can add members");

        conversationMemberRepository.deleteByConversation_IdAndMember_Id(conversation.getId(), dto.getMemberId());
        log.info("Remove member: {}", dto.getMemberId());
    }

    @Override
    public void updateConversation(UpdateNameConversation dto) throws AccessDeniedException {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if(!isAdmin) throw new AccessDeniedException("You are not allowed to change name group");

        conversation.setGroupName(dto.getConversationNewName());
        conversationRepository.save(conversation);
        log.info("Update conversation: {}", dto.getConversationNewName());
    }

    @Override
    public void updateConversationAvatar(Long conversationId, String avatarUrl) {

    }

    @Override
    public void updateMemberRole(UpdateMemberRole updateMemberRole) throws AccessDeniedException {
        Conversation conversation = conversationRepository.findById(updateMemberRole.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), updateMemberRole.getRequestId(), true);
        if(!isAdmin) throw new AccessDeniedException("You are not allowed to change roles");

        // 2. Tìm participant cần cập nhật
        ConversationMember target = conversationMemberRepository
                .findByConversation_IdAndMember_Id(updateMemberRole.getConversationId(), updateMemberRole.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this conversation"));
        // 3. Cập nhật quyền
        target.setAdmin(true);
        conversationMemberRepository.save(target);
        log.info("Update member: " + updateMemberRole.getMemberId());
    }

    @Override
    @Transactional
    public void leaveConversation(Long conversationId, Long userId) throws BadRequestException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        ConversationMember participant = conversationMemberRepository.findByConversation_IdAndMember_Id(conversationId, userId)
                .orElseThrow(() -> new BadRequestException("User is not a member of the conversation"));

        // Nếu là admin, kiểm tra xem có admin nào khác không
        if (participant.isAdmin()) {
            long adminCount = conversationMemberRepository.countByConversation_IdAndAdmin(conversationId, true);
            if (adminCount <= 1) {
                // khong co phai chuyen admin cho 1 ai khac
                throw new BadRequestException("Cannot leave conversation as the only admin. Please assign another admin.");
            }
        }
        // xoa nguoi dung
        conversationMemberRepository.delete(participant);

        // Nếu không còn ai trong nhóm, xoá nhóm luôn
        long memberCount =conversationMemberRepository.countByConversation_Id(conversationId);

        if (memberCount == 0) {
            conversationRepository.deleteById(conversationId);
        }
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        ConversationMember participant = conversationMemberRepository.findByConversation_IdAndMember_Id(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the conversation"));

        if (!participant.isAdmin()) {
            throw new InvalidDataException("Only admin can delete conversations");
        }
        conversationRepository.delete(conversation);
    }

    @Override
    @Transactional
    public List<MemberResponse> getConversationMembers(Long conversationId) {
        // Kiểm tra tồn tại cuộc trò chuyện
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        List<ConversationMember> conversationMembers = conversationMemberRepository.findByConversation_Id(conversationId);
        return conversationMembers.stream().map(conversationMemberMapper::toResponse).toList();
    }

    @Override
    public boolean isAdmin(Long conversationId, Long userId) {
        return false;
    }
}
