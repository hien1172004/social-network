package backend.example.mxh.DTO.request;

import lombok.Data;

@Data
public class UserStatusDTO {
    private Long userId;
    private String status;

    public UserStatusDTO(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }
}