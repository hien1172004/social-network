package backend.example.mxh.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MutualFriendResponse {
    private Long id;
    private String username;
    private String avatarUrl;
}
