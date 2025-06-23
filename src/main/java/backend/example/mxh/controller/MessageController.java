package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Gửi tin nhắn mới
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        return ResponseEntity.ok(messageService.sendMessage(messageDTO));
    }

    // Lấy danh sách tin nhắn theo conversationId cua user (có phân trang)
    @GetMapping("/conversation/{conversationId}/user/{userId}")
    public ResponseEntity<PageResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(messageService.getMessagesByConversationId(conversationId,userId, pageNo, pageSize));
    }

    // Đánh dấu một tin nhắn là đã đọc
    @PutMapping("/{messageId}/read/{userId}")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId, @PathVariable Long userId) {
        messageService.markMessageAsRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu tất cả tin nhắn trong một cuộc trò chuyện là đã đọc
    @PutMapping("/conversation/{conversationId}/read-all/{userId}")
    public ResponseEntity<Void> markAllMessagesAsRead(@PathVariable Long conversationId, @PathVariable Long userId) {
        messageService.markAllMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    // Đếm số tin nhắn chưa đọc trong cuộc trò chuyện
    @GetMapping("/conversation/{conversationId}/unread-count/{userId}")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable Long conversationId, @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.countUnreadMessages(conversationId, userId));
    }
}
