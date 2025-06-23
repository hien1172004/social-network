package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.response.MessageResponse;
import backend.example.mxh.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// MessageMapper.java
@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "conversation.id", source = "conversationId")
    @Mapping(target = "sender.id", source = "senderId")
    Message toMessage(MessageDTO dto);

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "revoked", source = "revoked")
    MessageResponse toResponse(Message message);

    // Bạn tự định nghĩa method này, KHÔNG dùng @Mapping
    default MessageResponse toResponse(Message message, Long userId) {
        MessageResponse response = toResponse(message); // dùng method MapStruct generate
        if(response.isRevoked()){
            response.setContent("Tin nhắn đã bị thu hồi");
        }
        boolean isRead = message.getStatuses() != null &&
                message.getStatuses().stream()
                        .anyMatch(status -> status.getUser().getId().equals(userId) && status.isRead());
        response.setRead(isRead);
        return response;
    }
}