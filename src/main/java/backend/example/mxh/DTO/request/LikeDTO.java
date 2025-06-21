package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LikeDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Post ID is required")
    private Long postId;
}
