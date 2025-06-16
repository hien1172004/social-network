package backend.example.mxh.mapper;

import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.entity.Conversation;
import backend.example.mxh.entity.ConversationMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMemberMapper {
    @Mapping(target = "username", source = "member.username")
    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "avatarUrl", source = "member.avatarUrl")
    @Mapping(target = "admin", source = "admin")
    MemberResponse toResponse(final ConversationMember member);
}
