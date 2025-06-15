package backend.example.mxh.DTO.request;

import lombok.Data;

@Data
public class RemoveMemberDTO {
    private Long conversationId;
    private Long memberId; // thành viên bị xóa
    private Long requesterId;
}