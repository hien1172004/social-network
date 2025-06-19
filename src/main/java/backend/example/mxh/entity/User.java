package backend.example.mxh.entity;

import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.UserRole;
import backend.example.mxh.until.UserStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private String avatarUrl;

    private String publicId;

    private String fullName;

    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    private LocalDateTime lastActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.INACTIVE;

    @OneToMany(mappedBy = "user")
    private List<Posts> posts;

    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    @OneToMany(mappedBy = "user")
    private List<Like> likes;

    @OneToMany(mappedBy = "user")
    private List<MessageStatus> messageStatuses;

    @OneToMany(mappedBy = "sender")
    private List<Friend> sentFriendRequests;

    @OneToMany(mappedBy = "receiver")
    private List<Friend> receivedFriendRequests;

    @OneToMany(mappedBy = "member")
    private List<ConversationMember> conversations;

    @OneToMany(mappedBy = "sender")
    private List<Notification> sentNotification;

    @OneToMany(mappedBy = "receiver")
    private List<Notification> receivedNotification;

}
