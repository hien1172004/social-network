package backend.example.mxh.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDTO {
    @NotBlank(message = "publicId is required")
    private String publicId;
    @NotBlank(message = "imageURl is required")
    private String imageUrl;
}
