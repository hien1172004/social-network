package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.EmailAlreadyExistsException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.UserMapper;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.BaseRedisService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UploadImageFile cloudinary;
    private final WebSocketService webSocketService;
    private final BaseRedisService<String, String, UserResponse> baseRedisService;
    private final BaseRedisService<String, String, PageResponse<List<UserResponse>>> baseRedisServicePage;

    private static final String USER_KEY = "user:";

    @Override
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("email not found"));
    }

    @Override
    @Transactional
    public long addUser(AddUserDTO userDTO) {
        if (userDTO == null || userDTO.getEmail() == null || userDTO.getUsername() == null) {
            throw new IllegalArgumentException("Dữ liệu người dùng không hợp lệ");
        }

        // Kiểm tra email hoặc username đã tồn tại
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã tồn tại");
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

        //xoa cache du lieu
        baseRedisService.delete(USER_KEY + id);
        log.info("updated user and cleared cache");
    }

    @Override
    @Transactional
    public void updateAvatar(long userId, ImageDTO dto) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        log.info("updating avatar:1 {}", user);
        try {
            if (user.getPublicId() != null && !user.getPublicId().isEmpty()
                    && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                cloudinary.deleteImage(user.getPublicId());
                log.info("deleted image from cloudinary");
            }

        } catch (Exception e) {
            log.warn("Failed to delete image from cloudinary: {}", e.getMessage());
        }
        log.info("updating avatar121221: {}", dto);
        user.setPublicId(dto.getPublicId());
        user.setAvatarUrl(dto.getImageUrl());
        userRepository.save(user);
        log.info("updated avatar");

        //xoa cache
        baseRedisService.delete(USER_KEY + userId);
        log.info("updated avatar and cleared cache");

    }

    @Override
    public UserResponse getDetailUser(long id) {
        String redisKey = USER_KEY + id;
        UserResponse cachedUser = baseRedisService.get(redisKey);
        if(cachedUser != null) {
            return cachedUser;
        }
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        log.info("get detail user");
        UserResponse userResponse =  userMapper.toUserResponse(user);

        //cache du lieu
        baseRedisService.set(redisKey, userResponse);
        baseRedisService.setTimeToLive(redisKey, 3600); // ttl 1h
        return userResponse;
    }

    @Override
    public PageResponse<List<UserResponse>> searchUser(int pageNo, int pageSize, String key) {

        String redisKey = "users:search:" + key + ":" + pageNo + "-" + pageSize;

        PageResponse<List<UserResponse>> cached = baseRedisServicePage.get(redisKey);
        if (cached != null) {
            log.info("Return search user from cache: {}", redisKey);
            return cached;
        }

        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "username"));
        Page<User> users;
        if(key != null && !key.isEmpty()){
            users = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(pageable, key, AccountStatus.ACTIVE);
        }
        else{
            users = userRepository.findAll(pageable);
        }

        PageResponse<List<UserResponse>> response = PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(users.stream().map(userMapper::toUserResponse).toList())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .build();

        baseRedisServicePage.set(redisKey, response);
        baseRedisServicePage.setTimeToLive(redisKey, 60); // TTL 1 phút
        return response;
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("deleted user");

        baseRedisService.delete(USER_KEY + id);
        log.info("deleted user in redis");


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

//        String redisKey = "users:online:" + pageNo + "-" + pageSize;
//        // Lấy cache
//        PageResponse<List<UserResponse>> cached = baseRedisServicePage.get(redisKey);
//        if (cached != null) {
//            log.info("Return users online from cache: {}", redisKey);
//            return cached;
//        }
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "lastActive"));
        Page<User> users = userRepository.findByStatus(backend.example.mxh.until.UserStatus.ONLINE, pageable);
//        // Lưu cache và đặt TTL 1 phút
//        baseRedisServicePage.set(redisKey, response);
//        baseRedisServicePage.setTimeToLive(redisKey, 60);
        return  PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(users.stream().map(userMapper::toUserResponse).toList())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .build();
    }


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

    @Override
    public PageResponse<List<UserResponse>> getAllUsers(int pageNo, int pageSize, String key, String... sorts) {
        int page = 0;
        if(pageNo > 0) {
            page = pageNo - 1;
        }
        List<Sort.Order> orders = new ArrayList<>();
        for(String sortBy : sorts) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()) {
                if(matcher.group(3).equalsIgnoreCase("asc")){
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
                else if(matcher.group(3).equalsIgnoreCase("desc")){
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));
        Page<User> users;
        if(key != null && !key.isEmpty()) {
            users = userRepository.getUserWithKeyword(key, pageable);
        }
        else {
            users = userRepository.findAll(pageable);
        }
        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(users.stream().map(userMapper::toUserResponse).toList())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .build();
    }

    @Override
    public void updateStatus(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        if(user.getAccountStatus() == AccountStatus.ACTIVE){
            user.setAccountStatus(AccountStatus.INACTIVE);
        }
        else if(user.getAccountStatus() == AccountStatus.INACTIVE){
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        userRepository.save(user);
        log.info("update user {} status {}", user.getUsername(), user.getStatus());

        //xoa cache
        baseRedisService.delete(USER_KEY + user.getId());
    }

}
