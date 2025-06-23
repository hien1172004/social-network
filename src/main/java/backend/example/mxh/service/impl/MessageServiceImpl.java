package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.DTO.response.MessageReadResponse;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.*;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.MessageMapper;
import backend.example.mxh.repository.*;
import backend.example.mxh.service.MessageService;

import backend.example.mxh.service.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageStatusRepository messageStatusRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final ConversationMemberRepository conversationMemberRepository;

    @Override
    public long sendMessage(MessageDTO messageDTO) {
        Conversation conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = messageMapper.toMessage(messageDTO);
        message.setConversation(conversation);
        message.setSender(sender);
        // Tạo MessageStatus cho tất cả thành viên trong cuộc trò chuyện
        Message finalMessage = message;
        List<MessageStatus> statuses = conversation.getMembers().stream()
                .map((ConversationMember member) -> {
                    MessageStatus status = new MessageStatus();
                    status.setMessage(finalMessage);
                    status.setUser(member.getMember());
                    status.setRead(member.getMember().getId().equals(sender.getId()));
                    return status;
                })
                .toList();
        message.setStatuses(statuses);

        message = messageRepository.save(message);
        MessageResponse response = messageMapper.toResponse(message);

        // Gửi tin nhắn qua WebSocket
        if (conversation.getMembers().size() > 2) {
            // Gửi theo nhóm
            webSocketService.sendMessage(conversation.getId(), response);
        } else {
            // 1-1 chat
            conversation.getMembers().stream()
                    .map(ConversationMember::getMember)
                    .filter(user -> !user.getId().equals(sender.getId()))
                    .forEach(user -> webSocketService.sendPrivateMessage(user.getId(), response));
        }

        return message.getId();
    }

    @Override
    public PageResponse<List<MessageResponse>> getMessagesByConversationId(Long conversationId, Long userId, int pageNo, int pageSize) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        ConversationMember member = conversationMemberRepository.findByConversation_IdAndMember_Id(conversationId, userId).orElseThrow(()
                -> new ResourceNotFoundException("User is not a member of conversation"));

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages;
        if (member.getLastDeletedAt() != null) {
            // Cần tạo hàm này trong MessageRepository
            messages = messageRepository.findByConversation_IdAndCreatedAtAfter(conversationId, member.getLastDeletedAt(), pageable);
        } else {
            messages = messageRepository.findByConversation_Id(conversationId, pageable);
        }

        return PageResponse.<List<MessageResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(messages.stream().map(message -> messageMapper.toResponse(message, userId)).toList())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
    }

    @Override
    public void markMessageAsRead(Long messageId, Long userId) {
        MessageStatus status = messageStatusRepository
                .findByMessage_IdAndUser_Id(messageId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageStatus not found"));

        if (!status.isRead()) {
            status.setRead(true);
            messageStatusRepository.save(status);
        }

        log.info("Message marked as read");
        // Gửi realtime cho những user còn lại trong conversation
        MessageReadResponse response = new MessageReadResponse(messageId, userId);
        List<ConversationMember> conversationMembers = status.getMessage().getConversation().getMembers();
        conversationMembers.stream()
                .map(ConversationMember::getMember)
                .filter(member -> !member.getId().equals(userId)) // loại người đã đọc
                .forEach(member -> webSocketService.sendReadMessageStatus(status.getMessage().getConversation().getId(), response));
    }

    @Override
    public void markAllMessagesAsRead(Long conversationId, Long userId) {
        List<MessageStatus> statuses = messageStatusRepository.getMessageNotRead(conversationId, userId);
        statuses.forEach(message -> message.setRead(true));
        messageStatusRepository.saveAll(statuses);
        log.info("All messages marked as read");

        // Gửi realtime tới các thành viên khác trong cuộc trò chuyện
        List<ConversationMember> members = conversationMemberRepository.findByConversation_Id(conversationId);
        List<User> otherUsers = members.stream()
                .map(ConversationMember::getMember)
                .filter(member -> !member.getId().equals(userId)) // loại người đã đọc
                .toList();

        for (MessageStatus status : statuses) {
            MessageReadResponse response = new MessageReadResponse(status.getMessage().getId(), userId);
            for (User other : otherUsers) {
                webSocketService.sendReadMessageStatus(conversationId, response);
            }
        }
    }

    @Override
    public long countUnreadMessages(Long conversationId, Long userId) {
        return messageStatusRepository.countMessageNotRead(conversationId, userId);
    }

    @Override
    public void revokeMessage(Long messageId, Long userId) throws AccessDeniedException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Kiểm tra quyền: chỉ cho phép người gửi hoặc admin thu hồi
        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("You can only revoke your own messages");
        }

        message.setRevoked(true);
        messageRepository.save(message);
        log.info("Message revoked");
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        MessageStatus status = messageStatusRepository.findByMessage_IdAndUser_Id(messageId, userId).orElseThrow(()
        -> new ResourceNotFoundException("MessageStatus not found"));
        status.setDeleted(true);
        messageStatusRepository.save(status);
        log.info("Message deleted");
    }
}
