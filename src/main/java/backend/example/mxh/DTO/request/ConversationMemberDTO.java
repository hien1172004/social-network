package backend.example.mxh.DTO.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberDTO {
    private Long userId;
    private String role; // ADMIN, MEMBER
    private boolean isActive;
}