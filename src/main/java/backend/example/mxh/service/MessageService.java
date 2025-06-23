package backend.example.mxh.service;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(MessageDTO messageDTO);
    PageResponse<List<MessageResponse>> getMessagesByConversationId(Long conversationId, Long userId,  int page, int size);
    void markMessageAsRead(Long messageId, Long userId);
    void markAllMessagesAsRead(Long conversationId, Long userId);
    long countUnreadMessages(Long conversationId, Long userId);

}