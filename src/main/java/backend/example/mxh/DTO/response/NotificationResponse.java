package backend.example.mxh.DTO.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String content;
    private String type;
    private boolean isRead;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private Long referenceId;
    private LocalDateTime createdAt;
}