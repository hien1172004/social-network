package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.ConversationDTO;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// ConversationMapper.java
@Mapper(componentModel = "spring")
public interface ConversationMapper {
    @Mapping(target = "members", ignore = true)
    Conversation toConversation(ConversationDTO dto);

    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponse toResponse(Conversation conversation);
}