package backend.example.mxh.DTO.response;

import backend.example.mxh.until.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private MessageType type;
    private Long conversationId;
    private UserResponse sender;
    private LocalDateTime createdAt;
    private boolean isRead;
}