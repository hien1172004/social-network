package backend.example.mxh.service;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface MessageService {
    long sendMessage(MessageDTO messageDTO);
    PageResponse<List<MessageResponse>> getMessagesByConversationId(Long conversationId, Long userId,  int page, int size);
    void markMessageAsRead(Long messageId, Long userId);
    void markAllMessagesAsRead(Long conversationId, Long userId);
    long countUnreadMessages(Long conversationId, Long userId);
    void revokeMessage(Long messageId, Long userId) throws AccessDeniedException;
    void deleteMessage(Long messageId, Long userId);
    PageResponse<List<MessageResponse>> searchMessageInConversation(Long conversationId, Long userId, int pageNo, int pageSize, String keyword);
}