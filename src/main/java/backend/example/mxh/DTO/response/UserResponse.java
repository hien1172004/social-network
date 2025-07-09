package backend.example.mxh.DTO.response;

import backend.example.mxh.until.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActive;
}
