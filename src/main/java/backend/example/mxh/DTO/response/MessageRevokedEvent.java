// src/main/java/backend/example/mxh/DTO/response/MessageRevokedEvent.java
package backend.example.mxh.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageRevokedEvent {
    private Long conversationId;
    private Long messageId;
    private Long revokedBy;
}