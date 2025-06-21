package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "post_images")
public class PostImage extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    private String publicId;
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Posts posts;


}