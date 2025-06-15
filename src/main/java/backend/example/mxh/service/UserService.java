package backend.example.mxh.service;

import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface UserService {
    void updateUser(long id, UpdateUserDTO updateUserDTO);

    void updateAvatar(long id, ImageDTO imageDTO) throws IOException;

    UserResponse getDetailUser(long id);

    UserResponse getCurrentUser();

    PageResponse<List<UserResponse>> searchUser(int pageNo, int pageSize, String key);

    void deleteUser(long id);
}
