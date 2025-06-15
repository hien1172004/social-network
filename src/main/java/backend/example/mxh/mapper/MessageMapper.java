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
    @Mapping(target = "isRead", expression = "java(message.getStatuses().stream().anyMatch(status -> status.isRead()))")
    MessageResponse toResponse(Message message);
}