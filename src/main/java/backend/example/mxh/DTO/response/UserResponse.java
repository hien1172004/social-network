package backend.example.mxh.DTO.response;

import backend.example.mxh.until.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String fullName;
    private String bio;
    private String status;
    private UserRole role;
    private LocalDateTime lastActive;
}
