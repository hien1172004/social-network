package backend.example.mxh.DTO.request;

import lombok.Data;

@Data
public class MarkReadDTO {
    private Long messageId;
    private Long userId;
    private Long conversationId;
}
