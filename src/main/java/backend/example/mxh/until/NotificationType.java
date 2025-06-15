package backend.example.mxh.until;

public enum NotificationType {
    LIKE("đã thích bài viết của bạn."),
    COMMENT("đã bình luận trên bài viết của bạn."),
    FRIEND_REQUEST("đã gửi lời mời kết bạn."),
    MESSAGE("đã gửi cho bạn một tin nhắn."),
    ACCEPT_REQUEST("đã chấp nhận lời mời kêt bạn từ bạn");
    private final String template;

    NotificationType(String template) {
        this.template = template;
    }

    public String buildContent(String senderName) {
        return senderName + " " + template;
    }
}
