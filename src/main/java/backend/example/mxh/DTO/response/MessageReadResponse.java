package backend.example.mxh.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageReadResponse {
    private Long messageId;
    private Long readerId;
}
