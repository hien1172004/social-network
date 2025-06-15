package backend.example.mxh.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateUserDTO {

    private String username;
    private String email;
    private String fullName;
    private String bio;
}
