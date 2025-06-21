package backend.example.mxh.entity;

import backend.example.mxh.until.MessageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Message extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nội dung tin nhắn
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType type = MessageType.TEXT;
    // Quan hệ: Tin nhắn thuộc về một cuộc trò chuyện
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // Quan hệ: Người gửi tin nhắn
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Quan hệ: Một tin nhắn có nhiều trạng thái đọc (1-n với MessageStatus)
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageStatus> statuses = new ArrayList<>();
}
