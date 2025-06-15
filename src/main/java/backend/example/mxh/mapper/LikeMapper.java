package backend.example.mxh.mapper;

import backend.example.mxh.DTO.response.LikeUserResponse;
import backend.example.mxh.entity.Like;
import backend.example.mxh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {
//    default LikeUserResponse toUserResponse(Like like) {
//        User user = like.getUser();
//        return new LikeUserResponse(
//            user.getId(),
//            user.getUsername(),
//            user.getAvatarUrl(),
//            user.getFullName()
//        );
//    }
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "username", source = "user.username")
   LikeUserResponse toLikeUserResponse(Like like);
}
