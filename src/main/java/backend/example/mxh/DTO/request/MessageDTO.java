package backend.example.mxh.DTO.request;

import backend.example.mxh.until.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String content;
    private MessageType type;
    private Long conversationId;
    private Long senderId;
    private LocalDateTime createdAt;
}