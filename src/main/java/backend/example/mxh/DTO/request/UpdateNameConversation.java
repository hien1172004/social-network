package backend.example.mxh.DTO.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNameConversation {
    private Long conversationId;
    private Long requesterId;
    private String conversationNewName;
}
