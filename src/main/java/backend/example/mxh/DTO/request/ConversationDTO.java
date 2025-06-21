package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConversationDTO {
    @NotNull(message = "Member IDs are required")
    private List<Long> memberIds;

    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;

    private long creatorId;

    private boolean isGroup;
}