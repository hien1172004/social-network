package backend.example.mxh.DTO.request;

import lombok.Data;

import java.util.List;

@Data
public class AddMemberDTO {
    private Long conversationId;
    private List<Long> memberIds;
    private Long requesterId; // người đang yêu cầu thêm
}