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
import backend.example.mxh.service.WebSocketService;
import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UploadImageFile cloudinary;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public long addUser(AddUserDTO userDTO) {
        if (userDTO == null || userDTO.getEmail() == null || userDTO.getUsername() == null) {
            throw new IllegalArgumentException("Dữ liệu người dùng không hợp lệ");
        }

        // Kiểm tra email hoặc username đã tồn tại
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        User user = userMapper.toUser2(userDTO);
        log.info("Adding user: {}", user);
        userRepository.save(user);
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        userMapper.updateUser(user, dto);
        userRepository.save(user);
        log.info("updated user");
    }

    @Override
    @Transactional
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
    @Transactional
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("deleted user");
    }

    @Override
    @Transactional
    public void setUserOnline(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        user.setStatus(backend.example.mxh.until.UserStatus.ONLINE);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);

        webSocketService.setOnlineStatus(user.getId());
        // Gửi thông báo WebSocket đến tất cả client
        log.info("Người dùng {} chuyển sang trạng thái ONLINE", user.getUsername());
    }

    @Override
    @Transactional
    public void setUserOffline(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        user.setStatus(backend.example.mxh.until.UserStatus.OFFLINE);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
        webSocketService.setOfflineStatus(user.getId());
        // Gửi thông báo WebSocket đến tất cả client
        log.info("Người dùng {} chuyển sang trạng thái OFFLINE", user.getUsername());
    }

    @Override
    public PageResponse<List<UserResponse>> getUsersOnline(int pageNo, int pageSize) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "username"));
        Page<User> users = userRepository.findByStatus(backend.example.mxh.until.UserStatus.ONLINE, pageable);
        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(users.stream().map(userMapper::toUserResponse).toList())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .build();
    }

//    @Override
//    public UserResponse getUserOnlineStatus(long id) {
//        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
//        if(user.getStatus().equals(backend.example.mxh.until.UserStatus.ONLINE) && user.getLastActive() != null){
//            if(user.getLastActive().isBefore(LocalDateTime.now().minusMinutes(5))){
//                user.setStatus(UserStatus.OFFLINE);
//                userRepository.save(user);
//                webSocketService.setOfflineStatus(user.getId());
//                log.info("Người dùng {} được đánh dấu OFFLINE do không hoạt động", user.getUsername());
//            }
//        }
//        return userMapper.toUserResponse(user);
//    }

    @Override
    @Transactional
    public void updateLastActiveTime(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        user.setLastActive(LocalDateTime.now());
        user.setStatus(backend.example.mxh.until.UserStatus.ONLINE);
        userRepository.save(user);
        log.info("Cập nhật thời gian hoạt động cho người dùng {}", userId);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoMarkUserOffline() {
        List<User> onlineUsers = userRepository.findByStatusAndLastActiveBefore(
                UserStatus.ONLINE, LocalDateTime.now().minusMinutes(5));

        if (onlineUsers.isEmpty()) return;

        for (User user : onlineUsers) {
            user.setStatus(UserStatus.OFFLINE);
        }

        userRepository.saveAll(onlineUsers); // ✅ Gọi 1 lần saveAll thay vì từng user

        // Gửi WebSocket thông báo trạng thái OFFLINE
        onlineUsers.forEach(user -> {
            webSocketService.setOfflineStatus(user.getId());
            log.info("Người dùng {} được đánh dấu OFFLINE do không hoạt động", user.getUsername());
        });
    }

}
