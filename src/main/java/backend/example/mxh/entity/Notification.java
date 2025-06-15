package backend.example.mxh.entity;

import backend.example.mxh.until.NotificationType;
import jakarta.persistence.*;
import lombok.*;

// Notification.java
@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type; // LIKE, COMMENT, FRIEND_REQUEST, MESSAGE

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "reference_id")
    private Long referenceId; // ID của bài viết, bình luận, tin nhắn, etc.
}

