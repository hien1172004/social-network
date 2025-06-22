package backend.example.mxh.DTO.response;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageResponse {
    public String publicId;
    public String imageUrl;
}
