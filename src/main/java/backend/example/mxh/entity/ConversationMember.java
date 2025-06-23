package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conversation_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
})
@Getter
@Setter
@SuperBuilder
public class ConversationMember extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ: thuộc về 1 cuộc trò chuyện
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // Quan hệ: là 1 người dùng
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User member;

    private boolean admin = false;

    private LocalDateTime lastDeletedAt;
}
