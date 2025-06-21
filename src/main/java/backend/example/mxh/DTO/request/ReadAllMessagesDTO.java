package backend.example.mxh.DTO.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadAllMessagesDTO {
    private Long conversationId;
    private Long userId;
}
