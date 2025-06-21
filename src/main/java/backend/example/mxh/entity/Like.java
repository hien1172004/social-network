package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Like extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người dùng đã thích bài viết
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Bài viết được thích
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Posts posts;
}
