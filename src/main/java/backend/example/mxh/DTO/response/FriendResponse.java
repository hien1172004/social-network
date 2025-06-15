package backend.example.mxh.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {
    private Long id;
    private Long senderId;
    private String senderUserName;
    private String senderAvatar;

    private Long receiverId;
    private String receiverUserName;
    private String receiverAvatar;

    private String status; // PENDING, ACCEPTED, DECLINED
}
