package backend.example.mxh.DTO.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private boolean isGroup;
    private String groupName;
    private List<MemberResponse> members;
    private MessageResponse lastMessage;
    private int unreadCount;
    private LocalDateTime updatedAt;
}