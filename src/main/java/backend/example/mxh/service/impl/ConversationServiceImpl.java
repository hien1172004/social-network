package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.*;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.*;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.ConversationMapper;
import backend.example.mxh.mapper.ConversationMemberMapper;
import backend.example.mxh.mapper.MessageMapper;
import backend.example.mxh.repository.*;
import backend.example.mxh.service.ConversationService;
import backend.example.mxh.until.MessageType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;


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
    private final MessageStatusRepository messageStatusRepository;
    private final MessageMapper messageMapper;
    @Override
    @Transactional
    public Long createConversation(ConversationDTO conversationDTO) {
        // Validate memberIds
        if (conversationDTO.getMemberIds() == null || conversationDTO.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("Cuộc trò chuyện phải có ít nhất một thành viên");
        }

        final int MAX_MEMBERS = 100;
        Set<Long> memberIds = new HashSet<>(conversationDTO.getMemberIds());

        // Đảm bảo creator luôn nằm trong danh sách
        memberIds.add(conversationDTO.getCreatorId());

        if (memberIds.size() > MAX_MEMBERS) {
            throw new IllegalArgumentException("Số lượng thành viên vượt quá giới hạn: " + MAX_MEMBERS);
        }

        // Lấy thông tin người dùng từ DB
        List<User> users = userRepository.findAllById(memberIds);
        if (users.size() != memberIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều thành viên không tồn tại");
        }

        // Xác định creator
        User creator = users.stream()
                .filter(u -> u.getId().equals(conversationDTO.getCreatorId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người tạo"));

        // Tạo entity Conversation
        Conversation conversation = conversationMapper.toConversation(conversationDTO);
        conversation.setGroup(memberIds.size() > 2); // Nếu hơn 2 người → group chat

        // Tạo danh sách thành viên
        List<ConversationMember> memberEntities = new ArrayList<>();

        for (User user : users) {
            ConversationMember member = new ConversationMember();
            member.setConversation(conversation);
            member.setMember(user);
            member.setAdmin(user.getId().equals(creator.getId())); // Creator là admin
            memberEntities.add(member);
        }

        // Gán memberEntities cho Conversation và lưu
        conversation.setMembers(memberEntities);
        conversation = conversationRepository.save(conversation);

        // Trả về response
        return conversation.getId();
    }

    @Override
    public ConversationResponse getConversationById(Long id, Long userId) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        Message message = messageRepository.findTopMessage(id);
        long unReadMessage = messageStatusRepository.countMessageNotRead(id, userId);

        ConversationResponse conversationResponse = conversationMapper.toResponse(conversation);
        conversationResponse.setUnreadCount((int) unReadMessage);
        conversationResponse.setLastMessage(messageMapper.toResponse(message));
        return conversationResponse;
    }

    @Override
    public PageResponse<List<ConversationResponse>> getConversationsByUserId(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Conversation> conversations = conversationRepository.findAllConversationsByUser_Id(userId, pageable);
        List<ConversationResponse> conversationResponses = conversations.stream().map(conversation -> {
            Message message = messageRepository.findTopMessage(conversation.getId());
            long unReadMessage = messageStatusRepository.countMessageNotRead(conversation.getId(), userId);
            ConversationResponse conversationResponse = conversationMapper.toResponse(conversation);
            conversationResponse.setUnreadCount((int) unReadMessage);
            conversationResponse.setLastMessage(messageMapper.toResponse(message));
            return conversationResponse;
        }).toList();
        return PageResponse.<List<ConversationResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(conversations.getTotalPages())
                .totalElements(conversations.getTotalElements())
                .items(conversationResponses)
                .build();
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
        // Tìm người thực hiện thêm
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        for (User user : users) {
            ConversationMember member = new ConversationMember();
            member.setConversation(conversation);
            member.setMember(user);
            member.setAdmin(false);
            conversationMemberRepository.save(member);

            //  Tạo tin nhắn hệ thống cho từng người được thêm
            Message systemMessage = Message.builder()
                    .conversation(conversation)
                    .sender(null) // hoặc để sender là người thêm cũng được
                    .content(requester.getUsername() + "đã thêm " + user.getFullName() + " cuộc trò chuyện.")
                    .type(MessageType.SYSTEM)
                    .build();

            messageRepository.save(systemMessage);
        }
        log.info("Add member to conversation");

    }

    @Override
    @Transactional
    public void removeMemberFromConversation(RemoveMemberDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        // Kiểm tra xem thành viên có tồn tại không
        boolean memberExists = conversationMemberRepository.existsByConversation_IdAndMember_Id(conversation.getId(), dto.getMemberId());
        if (!memberExists) {
            log.warn("No member found with conversationId: {} and memberId: {}", conversation.getId(), dto.getMemberId());
            throw new ResourceNotFoundException("Member not found in conversation");
        }
        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if (!isAdmin) throw new InvalidDataException("Only admin can add members");
        // Tìm người thực hiện và người bị xóa
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        User target = userRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Target member not found"));

        conversationMemberRepository.deleteByConversation_IdAndMember_Id(conversation.getId(), dto.getMemberId());
        log.info("Remove member: {}", dto.getMemberId());

        // Gửi tin nhắn hệ thống
        Message systemMessage = Message.builder()
                .conversation(conversation)
                .sender(null) // hệ thống
                .type(MessageType.SYSTEM)
                .content(requester.getFullName() + " đã xoá " + target.getFullName() + " khỏi cuộc trò chuyện.")
                .build();

        messageRepository.save(systemMessage);
    }

    @Override
    public void updateConversation(UpdateNameConversation dto) throws AccessDeniedException {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), dto.getRequesterId(), true);
        if(!isAdmin) throw new AccessDeniedException("You are not allowed to change name group");
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        conversation.setGroupName(dto.getGroupName());
        conversationRepository.save(conversation);
        log.info("Update conversation: {}", dto.getGroupName());

        // Gửi tin nhắn hệ thống
        Message systemMessage = Message.builder()
                .conversation(conversation)
                .sender(null) // hệ thống
                .type(MessageType.SYSTEM)
                .content(requester.getFullName() + " đã xoá đổi tên nhóm thành " + dto.getGroupName())
                .build();

        messageRepository.save(systemMessage);
    }


    @Override
    public void updateMemberRole(UpdateMemberRole updateMemberRole) throws AccessDeniedException {
        Conversation conversation = conversationRepository.findById(updateMemberRole.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isAdmin = conversationMemberRepository.existsByConversation_IdAndMember_IdAndAdmin(conversation.getId(), updateMemberRole.getRequestId(), true);
        if(!isAdmin) throw new AccessDeniedException("You are not allowed to change roles");

        //  Không cho phép admin tự hạ quyền chính mình
        if (Objects.equals(updateMemberRole.getRequestId(), updateMemberRole.getMemberId())) {
            throw new InvalidDataException("You cannot change your own role");
        }

        // Tìm participant cần cập nhật
        ConversationMember target = conversationMemberRepository
                .findByConversation_IdAndMember_Id(updateMemberRole.getConversationId(), updateMemberRole.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this conversation"));



        // 3. Cập nhật quyền
        boolean newAdminStatus = !target.isAdmin();
        target.setAdmin(newAdminStatus);
        conversationMemberRepository.save(target);
        log.info("Updated member {} to admin = {}", target.getMember().getId(), newAdminStatus);
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
        // Tạo tin nhắn hệ thống thông báo người dùng rời khỏi nhóm
        Message systemMessage = Message.builder()
                .conversation(conversation)
                .sender(null) // Tin nhắn hệ thống, có thể để null hoặc người rời nhóm
                .content(participant.getMember().getUsername() + " đã rời khỏi cuộc trò chuyện.")
                .type(MessageType.SYSTEM) // Tạo ENUM SYSTEM nếu chưa có
                .build();

        messageRepository.save(systemMessage);
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
    public PageResponse<List<MemberResponse>> getConversationMembers(Long conversationId, int pageNo, int pageSize) {
        // Kiểm tra tồn tại cuộc trò chuyện
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "member.username"));
        Page<ConversationMember> conversationMembers = conversationMemberRepository.findByConversation_Id(conversationId, pageable);
        return PageResponse.<List<MemberResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(conversationMembers.getTotalElements())
                .totalPages(conversationMembers.getTotalPages())
                .items(conversationMembers.stream().map(conversationMemberMapper::toResponse).toList())
                .build();
    }

    @Override
    public PageResponse<List<MemberResponse>> findMemBerInConversation(Long conversationId, int pageNo, int pageSize, String keyword) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "member.username"));

        Page<ConversationMember> conversationMembers = conversationMemberRepository.findMemberInConversation(conversationId, keyword, pageable);

        return PageResponse.<List<MemberResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(conversationMembers.getTotalElements())
                .totalPages(conversationMembers.getTotalPages())
                .items(conversationMembers.stream().map(conversationMemberMapper::toResponse).toList())
                .build();
    }


}
