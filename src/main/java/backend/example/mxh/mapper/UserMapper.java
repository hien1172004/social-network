package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.MutualFriendResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UpdateUserDTO updateUserDTO);

    User toUser2(AddUserDTO addUserDTO);
    MutualFriendResponse toMutualFriendResponse(User user);
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UpdateUserDTO updateUserDTO);
}
