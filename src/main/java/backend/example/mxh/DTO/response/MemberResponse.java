package backend.example.mxh.DTO.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private boolean admin;
}
