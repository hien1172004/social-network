package backend.example.mxh.DTO.request;

import backend.example.mxh.until.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Message content must not exceed 1000 characters")
    private String content;
    @NotNull(message = "Message type is required")
    private MessageType type;
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    @NotNull(message = "Sender ID is required")
    private Long senderId;
}