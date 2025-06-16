package backend.example.mxh.DTO.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMemberRole {
    private long conversationId;
    private long requestId;
    private long memberId;
}
