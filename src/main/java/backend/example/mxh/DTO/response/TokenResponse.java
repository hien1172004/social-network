package backend.example.mxh.DTO.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class TokenResponse implements Serializable {

    private String accessToken;

    private String refreshToken;

    private Long userId;
}
