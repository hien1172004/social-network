package backend.example.mxh.service;

import backend.example.mxh.DTO.request.UserStatusDTO;
import backend.example.mxh.DTO.response.MessageReadResponse;
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

    public void setOnlineStatus(long userId) {
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusDTO(userId, "ONLINE"));
        log.info("Gửi trạng thái ONLINE cho user {}", userId);
    }

    public void setOfflineStatus(long userId) {
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusDTO(userId, "OFFLINE"));
        log.info("Gửi trạng thái OFFLINE cho user {}", userId);
    }

    public void sendReadMessageStatus(Long conversationId, MessageReadResponse response) {
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/read", response);
    }


}