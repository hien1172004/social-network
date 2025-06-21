package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateMemberRole {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "User ID is required")
    private Long requestId;
}
