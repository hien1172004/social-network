package backend.example.mxh.service;

import backend.example.mxh.DTO.request.UserStatusDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.DTO.response.NotificationResponse;
import backend.example.mxh.DTO.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// WebSocketService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationResponse notification) {
        // Gửi thông báo đến một user cụ thể
        messagingTemplate.convertAndSendToUser(
            notification.getReceiverId().toString(),
            "/queue/notifications",
            notification
        );
    }

    public void sendNotificationToAll(NotificationResponse notification) {
        // Gửi thông báo đến tất cả user
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public void sendMessage(Long conversationId, MessageResponse message) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
    }

    public void sendPrivateMessage(Long userId, MessageResponse message) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/messages", message);
    }

    public void setOnlineStatus(UserResponse user) {
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusDTO(user.getId(), "ONLINE"));
        log.info("Gửi trạng thái ONLINE cho user {}", user.getId());
    }

    public void setOfflineStatus(UserResponse user) {
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusDTO(user.getId(), "OFFLINE"));
        log.info("Gửi trạng thái OFFLINE cho user {}", user.getId());
    }
}