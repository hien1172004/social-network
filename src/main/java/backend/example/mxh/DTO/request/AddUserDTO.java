package backend.example.mxh.DTO.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddUserDTO {
    private String username;
    private String fullname;
    private String phoneNumber;
    private String email;
    private String bio;
}
