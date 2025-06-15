package backend.example.mxh.DTO.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendDTO {
    private Long senderId;
    private Long receiverId;
}
