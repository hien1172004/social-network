package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.ConversationDTO;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.entity.Conversation;

import backend.example.mxh.entity.ConversationMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// ConversationMapper.java
@Mapper(componentModel = "spring")
public interface ConversationMapper {
    @Mapping(target = "members", ignore = true)
    Conversation toConversation(ConversationDTO dto);

    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "isGroup", source = "group")
    ConversationResponse toResponse(Conversation conversation);

    @Mapping(target = "username", source = "member.username")
    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "avatarUrl", source = "member.avatarUrl")
    @Mapping(target = "admin", source = "admin")
    MemberResponse toResponse(final ConversationMember member);


}