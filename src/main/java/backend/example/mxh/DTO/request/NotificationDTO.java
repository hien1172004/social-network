package backend.example.mxh.DTO.request;

import backend.example.mxh.until.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NotificationDTO {
    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Notification content must not exceed 500 characters")
    private String content;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "referenceId is required")
    private Long referenceId;
}