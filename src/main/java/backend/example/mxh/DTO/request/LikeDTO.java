package backend.example.mxh.DTO.request;

import lombok.Data;

@Data
public class LikeDTO {
    private Long userId;
    private Long postId;
}
