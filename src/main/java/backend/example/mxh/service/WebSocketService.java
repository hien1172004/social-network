package backend.example.mxh.service;

import backend.example.mxh.DTO.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// WebSocketService.java
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationResponse notification) {
        // Gửi thông báo đến một user cụ thể
        messagingTemplate.convertAndSendToUser(
            notification.getReceiverId().toString(),
            "/topic/notifications",
            notification
        );
    }

    public void sendNotificationToAll(NotificationResponse notification) {
        // Gửi thông báo đến tất cả user
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}