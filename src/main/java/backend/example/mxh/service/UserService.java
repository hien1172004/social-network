package backend.example.mxh.service;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;


public interface UserService {

    UserDetailsService userDetailsService();

    long addUser(AddUserDTO user);

    void updateUser(long id, UpdateUserDTO updateUserDTO);

    void updateAvatar(long id, ImageDTO imageDTO) throws IOException;

    UserResponse getDetailUser(long id);

    UserResponse getCurrentUser();

    PageResponse<List<UserResponse>> searchUser(int pageNo, int pageSize, String key);

    void deleteUser(long id);

    void setUserOnline(long id);

    void setUserOffline(long id);

    PageResponse<List<UserResponse>> getUsersOnline(int pageNo, int pageSize);

//    UserResponse getUserOnlineStatus(long id);

    void updateLastActiveTime(long id);

    void autoMarkUserOffline();

    PageResponse<List<UserResponse>> getAllUsers(int pageNo, int pageSize, String key, String... sortedBy);

    void updateStatus(long id);

}
