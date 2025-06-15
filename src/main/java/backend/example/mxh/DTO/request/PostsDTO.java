package backend.example.mxh.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PostsDTO {
    private String content;

    private long userId;

    private List<ImageDTO> postImage;
}
