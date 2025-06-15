package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Posts extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "posts")
    private List<PostImage> postImage;

    @OneToMany(mappedBy = "posts")
    private List<Comment> comments;

    @OneToMany(mappedBy = "posts")
    private List<Like> likes;

}