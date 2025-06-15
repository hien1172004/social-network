package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.*;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.MessageMapper;
import backend.example.mxh.repository.ConversationRepository;
import backend.example.mxh.repository.MessageRepository;
import backend.example.mxh.repository.MessageStatusRepository;
import backend.example.mxh.repository.UserRepository;
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

    @Override
    public MessageResponse sendMessage(MessageDTO messageDTO) {
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
                .map(member -> MessageStatus.builder()
                        .message(finalMessage)
                        .user(member.getMember())
                        .isRead(member.getMember().getId().equals(sender.getId()))
                        .build())
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

        return response;
    }

    @Override
    public PageResponse<List<MessageResponse>> getMessagesByConversationId(Long conversationId, int pageNo, int pageSize) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findByConversation_Id(conversationId, pageable);
        return PageResponse.<List<MessageResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(messages.stream().map(messageMapper::toResponse).toList())
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
    }

    @Override
    public void markAllMessagesAsRead(Long conversationId, Long userId) {
        List<MessageStatus> statuses = messageStatusRepository.getMessageNotRead(conversationId, userId);
        statuses.forEach(message -> message.setRead(true));
        messageStatusRepository.saveAll(statuses);
        log.info("All messages marked as read");
    }

    @Override
    public long countUnreadMessages(Long conversationId, Long userId) {
        return messageStatusRepository.countMessageNotRead(conversationId, userId);
    }
}
