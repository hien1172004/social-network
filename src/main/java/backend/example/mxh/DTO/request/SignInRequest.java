package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @NotBlank(message = "user must be not null")
    @Email
    private  String email;
    @NotBlank(message = "password must be not null")
    private String password;
}
