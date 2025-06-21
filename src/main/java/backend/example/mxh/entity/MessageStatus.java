package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "message_status", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MessageStatus extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ: Thuộc về 1 tin nhắn
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // Quan hệ: Người dùng đã đọc hay chưa
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Trạng thái: Đã đọc hay chưa
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
}
