package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddMemberDTO {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Member IDs are required")
    private List<Long> memberIds;

    @NotNull(message = "Requester ID is required")
    private Long requesterId; // người đang yêu cầu thêm
}