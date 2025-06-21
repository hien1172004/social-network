package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateNameConversation {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;

    @NotNull(message = "User ID is required")
    private Long userId;
}
