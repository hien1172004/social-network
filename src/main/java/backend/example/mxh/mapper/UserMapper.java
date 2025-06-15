package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UpdateUserDTO updateUserDTO);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UpdateUserDTO updateUserDTO);
}
