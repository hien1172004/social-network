package backend.example.mxh.DTO.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String groupName;
    private List<Long> memberIds;
    private Long creatorId;
}