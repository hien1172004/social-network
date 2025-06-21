package backend.example.mxh.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadMessageDTO {
    private Long userId;
    private Long messageId;
}
