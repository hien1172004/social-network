package backend.example.mxh.controller;

import backend.example.mxh.DTO.response.NotificationResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.NotificationService;
import backend.example.mxh.until.ResponseCode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Lấy tất cả thông báo theo userId (có phân trang)
    @GetMapping("/{userId}")
    public ResponseEntity<PageResponse<List<NotificationResponse>>> getNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(pageNo, pageSize, userId));
    }

    // Lấy tất cả thông báo chưa đọc theo userId (có phân trang)
    @GetMapping("/{userId}/unread")
    public ResponseEntity<PageResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUserId(pageNo, pageSize, userId));
    }

    // Đánh dấu 1 thông báo là đã đọc
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ResponseData<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "notification read"));
    }

    // Đánh dấu tất cả thông báo là đã đọc
    @PutMapping("/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // Đếm số lượng thông báo chưa đọc
    @GetMapping("/{userId}/count-unread")
    public ResponseEntity<Long> countUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnreadNotifications(userId));
    }
}
