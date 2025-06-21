package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "comments")
public class Comment extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nội dung bình luận
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Mỗi bình luận thuộc về 1 user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mỗi bình luận thuộc về 1 bài viết
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Posts posts;
}
