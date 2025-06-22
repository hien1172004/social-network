package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.entity.Posts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface PostsMapper {
    // Ánh xạ từ PostsDTO sang Posts
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "postImage", source = "postImage")
    Posts toPosts(PostsDTO postsDTO);

    // Ánh xạ từ Posts sang PostsResponse
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "likeQuantity", expression = "java(posts.getLikes() != null ? Long.valueOf(posts.getLikes().size()) : 0L)")
    @Mapping(target = "commentQuantity", expression = "java(posts.getComments() != null ? Long.valueOf(posts.getComments().size()) : 0L)")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "postImage", ignore = true)
    PostsResponse toPostsResponse(Posts posts);

    // Cập nhật Posts từ PostsDTO
    @Mapping(target = "id", ignore = true) // Không cập nhật ID
    @Mapping(target = "user", ignore = true) // Không cập nhật user (hoặc tùy logic)
    @Mapping(target = "postImage", ignore = true)
    void updatePosts(@MappingTarget Posts posts, PostsDTO postsDTO);

}
