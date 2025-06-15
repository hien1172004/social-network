package backend.example.mxh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "conversations")
@Builder
public class Conversation extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Xác định có phải nhóm hay trò chuyện riêng tư
    @Column(name = "is_group")
    @Builder.Default
    private boolean isGroup = false;

    // Tên nhóm (chỉ dùng nếu là group)
    @Column(name = "group_name")
    private String groupName;

    // Mối quan hệ: 1 cuộc trò chuyện có nhiều thành viên
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationMember> members = new ArrayList<>();

    // Mối quan hệ: 1 cuộc trò chuyện có nhiều tin nhắn
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();
}