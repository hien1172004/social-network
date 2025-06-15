package backend.example.mxh.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeUserResponse {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String fullName;
}
