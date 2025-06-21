package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CommentDTO {
    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Comment content must not exceed 500 characters")
    private String content;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Post ID is required")
    private Long postId;
}
