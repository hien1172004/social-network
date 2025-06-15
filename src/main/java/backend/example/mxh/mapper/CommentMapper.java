package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.CommentDTO;
import backend.example.mxh.DTO.response.CommentResponse;
import backend.example.mxh.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "posts.id", source = "postId")
    Comment toComment(CommentDTO dto);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "posts.id", source = "postId")
    void updateComment(@MappingTarget Comment comment, CommentDTO dto);
}
