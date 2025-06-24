package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.MessageService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Gửi tin nhắn mới
    @PostMapping
    public ResponseEntity<ResponseData<Long>> sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "gửi tin nhắn thành công", messageService.sendMessage(messageDTO)));
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
    public ResponseEntity<ResponseData<Void>> markMessageAsRead(@PathVariable Long messageId, @PathVariable Long userId) {
        messageService.markMessageAsRead(messageId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "tin  nhẵn đã được đọc"));
    }

    // Đánh dấu tất cả tin nhắn trong một cuộc trò chuyện là đã đọc
    @PutMapping("/conversation/{conversationId}/read-all/{userId}")
    public ResponseEntity<ResponseData<Void>> markAllMessagesAsRead(@PathVariable Long conversationId, @PathVariable Long userId) {
        messageService.markAllMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tất cả tin nhẵn đã được đọc"));
    }
    // Thu hồi tin nhắn
    @PutMapping("/revoke/message/{messageId}/user/{userId}")
    public ResponseEntity<ResponseData<Void>> revokeMessage(@PathVariable Long messageId, @PathVariable Long userId) throws AccessDeniedException {
        messageService.revokeMessage(messageId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tin nhắn đã được thu hồi"));
    }
    @PutMapping("/delete/message/{messageId}/user/{userId}")
    public ResponseEntity<ResponseData<Void>> deleteMessageForUser(@PathVariable Long messageId, @PathVariable Long userId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tin nhắn đã được thu hồi"));
    }
    // Đếm số tin nhắn chưa đọc trong cuộc trò chuyện
    @GetMapping("/conversation/{conversationId}/unread-count/{userId}")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable Long conversationId, @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.countUnreadMessages(conversationId, userId));
    }

    @GetMapping("/conversation/{conversationId}/user/{userId}/search")
    public ResponseEntity<ResponseData<PageResponse<List<MessageResponse>>>> searchMessagesInConversation(@PathVariable Long conversationId, @PathVariable Long userId,
                                                                                            @RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                            @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                                                            @RequestParam(required = true) String key){

        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "thành công", messageService.searchMessageInConversation(conversationId, userId, pageNo, pageSize, key)));
    }

}
