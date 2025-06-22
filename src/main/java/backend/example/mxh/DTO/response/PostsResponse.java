package backend.example.mxh.DTO.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostsResponse {
    private Long id;
    private String content;
    private Long userId;
    private Long likeQuantity;
    private Long commentQuantity;
    private List<ImageResponse> postImage;
    private LocalDateTime createdAt;
}
