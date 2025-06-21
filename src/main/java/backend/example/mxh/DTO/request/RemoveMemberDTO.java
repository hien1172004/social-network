package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RemoveMemberDTO {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Member ID is required")
    private Long memberId; // thành viên bị xóa

    @NotNull(message = "Requester ID is required")
    private Long requesterId;
}