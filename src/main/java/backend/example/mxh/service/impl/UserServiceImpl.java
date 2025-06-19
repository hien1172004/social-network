package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.UserMapper;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.UploadImageFile;
import backend.example.mxh.service.UserService;
import backend.example.mxh.until.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UploadImageFile cloudinary;

    @Override
    public long addUser(AddUserDTO userDTO) {
        User user = userMapper.toUser2(userDTO);
        log.info("Adding user: {}", user);
        return user.getId();
    }

    @Override
    public void updateUser(long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        userMapper.updateUser(user, dto);
        userRepository.save(user);
        log.info("updated user");
    }

    @Override
    public void updateAvatar(long userId, ImageDTO dto) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        if(!user.getPublicId().isEmpty()){
            cloudinary.deleteImage(user.getPublicId());
        }
        user.setPublicId(dto.getPublicId());
        user.setAvatarUrl(dto.getImageUrl());
        userRepository.save(user);
        log.info("updated avatar");
    }

    @Override
    public UserResponse getDetailUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        log.info("get detail user");
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails userDetails) {
//            // Chuyển đổi thành UserDetails
//            String email = userDetails.getUsername();
//
//            User user = userService.getByEmail(email);
//
//            return userMapper.toUserResponse(user);
//        } else {
//            // Trường hợp nếu principal không phải là UserDetails (chẳng hạn như String)
//            throw new BadCredentialsException("Không tìm thấy thông tin người dùng.");
//        }
        return null;
    }

    @Override
    public PageResponse<List<UserResponse>> searchUser(int pageNo, int pageSize, String key) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "username"));
        Page<User> users;
        if(key != null && !key.isEmpty()){
            users = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(pageable, key);
        }
        else{
            users = userRepository.findAll(pageable);
        }
        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(users.stream().map(userMapper::toUserResponse).toList())
                .totalPages(users.getTotalPages())
                .totalElements( users.getTotalElements())
                .build();
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("deleted user");
    }
}
