package backend.example.mxh.DTO.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String content;
    private String type;
    private Long senderId;
    private Long receiverId;
    private Long referenceId;
}