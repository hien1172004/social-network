package backend.example.mxh.service;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.EmailAlreadyExistsException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.UserMapper;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.impl.UserServiceImpl;
import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.UserRole;
import backend.example.mxh.until.UserStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;


@Slf4j
@ExtendWith(MockitoExtension.class)
@TestPropertySource("/test.properties")
public class UserServiceTest {

    private @Mock UserRepository userRepository;
    private @Mock UserMapper userMapper;
    private @Mock UploadImageFile cloudinary;
    private @Mock WebSocketService webSocketService;
    private @Mock BaseRedisService<String, String, Object> baseRedisService;
    private @Mock ObjectMapper redisObjectMapper;
    private UserService userService;
    private static final String USER_KEY = "user:";
    private User user;

    private AddUserDTO addUserDTO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UpdateUserDTO updateUserDTO;

    private ImageDTO imageDTO;

    private UserResponse userResponse;

    private PageResponse<List<UserResponse>> pageResponse;
    private UserResponse user1;
    private UserResponse user2;
    @BeforeEach
    void initData() {
        userService = new UserServiceImpl(userRepository, userMapper, cloudinary, webSocketService, baseRedisService, redisObjectMapper);

        addUserDTO = AddUserDTO.builder()
                .username("john")
                .fullName("John Doe")
                .email("john.doe123@example.com")
                .phoneNumber("0342610712")
                .password("admin123")
                .bio("hello")
                .build();

        user = User.builder()
                .id(1L)
                .username("john")
                .fullName("John Doe")
                .email("john.doe123@example.com")
                .phoneNumber("0342610712")
                .password("admin123")
                .bio("hello")
                .avatarUrl("https://i.pinimg.com/originals/c6/e5/65/c6e56503cfdd87da299f72dc416023d4.jpg")
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .status(UserStatus.OFFLINE)
                .build();

        updateUserDTO = UpdateUserDTO.builder()
                .email("john.doe123@example.com")
                .phoneNumber("0342610713")
                .fullName("John Doe143")
                .username("john week")
                .bio("Updated bio")
                .build();
        imageDTO = ImageDTO.builder().imageUrl("http://res.cloudinary.com/dw9krx7ac/image/upload/bac10bdb-bb7b-4c51-a901-cb28bf544cc3_OIP.jpg")
                .publicId("bac10bdb-bb7b-4c51-a901-cb28bf544cc3").build();

        // Khởi tạo UserResponse mẫu
        userResponse = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar.jpg")
                .bio("Hello, I'm John!")
                .status("ONLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();
        // Khởi tạo UserResponse mẫu
       user1 = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar1.jpg")
                .bio("Hello, I'm John!")
                .status("ONLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();

       user2 = UserResponse.builder()
                .id(2L)
                .username("jane_doe")
                .email("jane.doe@example.com")
                .fullName("Jane Doe")
                .avatarUrl("http://example.com/avatar2.jpg")
                .bio("Hello, I'm Jane!")
                .status("OFFLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 40, 0))
                .build();

        // Khởi tạo PageResponse mẫu
        pageResponse = PageResponse.<List<UserResponse>>builder()
                .pageNo(1)
                .pageSize(10)
                .items(List.of(user1, user2))
                .totalPages(1)
                .totalElements(2)
                .build();
    }

    @Test
    void addUser_validRequest_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser2(any())).thenReturn(user);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Giả lập DB tự sinh id
            return savedUser;
        });

        long userId = userService.addUser(addUserDTO);

        assertEquals(1L, userId);
    }

    @Test
    void addUser_emailAlreadyExists_fail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        var exception = Assertions.assertThrows(EmailAlreadyExistsException.class, () -> userService.addUser(addUserDTO));

        assertEquals("Email đã tồn tại", exception.getMessage());
    }

    @Test
    void updateUser_validRequest_success() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            UpdateUserDTO dto = invocation.getArgument(1);
            u.setUsername(dto.getUsername());
            u.setFullName(dto.getFullName());
            u.setPhoneNumber(dto.getPhoneNumber());
            u.setBio(dto.getBio());
            return null;
        }).when(userMapper).updateUser(any(User.class), any(UpdateUserDTO.class));

        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(baseRedisService).delete(USER_KEY + userId);

        userService.updateUser(userId, updateUserDTO);

        assertEquals(updateUserDTO.getUsername(), user.getUsername());
        assertEquals(updateUserDTO.getFullName(), user.getFullName());
        assertEquals(updateUserDTO.getPhoneNumber(), user.getPhoneNumber());
        assertEquals(updateUserDTO.getBio(), user.getBio());
    }

    @Test
    void updateUser_NotFound_fail() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(anyLong(), updateUserDTO));

        assertEquals("not found user", exception.getMessage());
    }
    @Test
    void updateAvatar_validRequestWithoutOldAvatar_success() throws IOException {
        // GIVEN
        long userId = 1L;
        user.setPublicId(null); // User không có ảnh cũ
        user.setAvatarUrl(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(baseRedisService).delete(USER_KEY + userId);

        // WHEN
        userService.updateAvatar(userId, imageDTO);

        // THEN
        verify(userRepository, times(1)).findById(userId);
        verify(cloudinary, never()).deleteImage(anyString());
        verify(userRepository, times(1)).save(user);
        verify(baseRedisService, times(1)).delete(USER_KEY + userId);

        // Kiểm tra trạng thái của user sau khi cập nhật
        assertEquals("bac10bdb-bb7b-4c51-a901-cb28bf544cc3", user.getPublicId());
        assertEquals("http://res.cloudinary.com/dw9krx7ac/image/upload/bac10bdb-bb7b-4c51-a901-cb28bf544cc3_OIP.jpg", user.getAvatarUrl());
    }
    @Test
    void updateAvatar_AvatarBlank_fail() throws IOException {
        long userId = 1L;
        user.setPublicId("");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(baseRedisService).delete(USER_KEY + userId);

        userService.updateAvatar(userId, imageDTO);

        // Kiểm tra trạng thái của user sau khi cập nhật
        assertEquals("bac10bdb-bb7b-4c51-a901-cb28bf544cc3", user.getPublicId());
        assertEquals("http://res.cloudinary.com/dw9krx7ac/image/upload/bac10bdb-bb7b-4c51-a901-cb28bf544cc3_OIP.jpg", user.getAvatarUrl());
    }
    @Test
    void updateAvatar_validRequestWithOldAvatar_success() throws IOException {
        // GIVEN
        user.setPublicId("old-public-id");
        user.setAvatarUrl("http://old-avatar-url.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(cloudinary).deleteImage(user.getPublicId());
        when(userRepository.save(any())).thenReturn(user);
        doNothing().when(baseRedisService).delete(USER_KEY + user.getId());
        // WHEN
        userService.updateAvatar(1L, imageDTO);

        // THEN
        Assertions.assertEquals(imageDTO.getPublicId(), user.getPublicId());
        Assertions.assertEquals(imageDTO.getImageUrl(), user.getAvatarUrl());
        // Kiểm tra cloudinary.deleteImage được gọi
        verify(cloudinary).deleteImage("old-public-id");
        // Kiểm tra xóa cache
        verify(baseRedisService).delete("user:1");
    }
    @Test
    void updateAvatar_notFoundUser_fail() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateAvatar(1L, imageDTO));
        assertEquals("not found user", exception.getMessage());
    }
    @Test
    void updateAvatar_deleteImageThrowsException_stillUpdates() throws IOException {
        // Given
        user.setPublicId("abc123");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("cloudinary fail"))
                .when(cloudinary).deleteImage(anyString());
        when(userRepository.save(any())).thenReturn(user);

        // When
        userService.updateAvatar(1L, imageDTO);

        // Then
        verify(cloudinary).deleteImage("abc123"); // vẫn gọi
        verify(userRepository).save(user);        // vẫn lưu user mới
        // Không fail vì exception đã được catch
    }

    @Test
    void getDetailUser_cacheHit_success() {
        long userId = 1L;
        String redisKey = USER_KEY + userId;

        Object cachedRaw = new Object(); // Object bất kỳ, vì bạn mock `convertValue` luôn
        when(baseRedisService.get(redisKey)).thenReturn(cachedRaw);
        when(redisObjectMapper.convertValue(cachedRaw, UserResponse.class)).thenReturn(userResponse);

        UserResponse result = userService.getDetailUser(userId);

        assertNotNull(result);
        assertEquals(userResponse, result);
    }

    @Test
    void getDetailUser_cacheMiss_success() {
        long userId = 1L;
        String redisKey = USER_KEY + userId;
        when(baseRedisService.get(redisKey)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        doNothing().when(baseRedisService).set(redisKey, userResponse);
        doNothing().when(baseRedisService).setTimeToLive(redisKey, 3600);
        UserResponse result = userService.getDetailUser(userId);

        assertNotNull(result);
        assertEquals(userResponse, result);


    }

    @Test
    void getDetailUser_NotFoundUser_fail() {
        long userId = 1L;
        String redisKey = USER_KEY + userId;
        when(baseRedisService.get(redisKey)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        var exception = Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.getDetailUser(userId));

        assertEquals("not found user", exception.getMessage());
    }

    @Test
    void searchUser_cacheHit_success() {
        int pageNo = 1;
        int pageSize = 10;
        String key = "john";
        String redisKey = "users:search:" + key + ":" + pageNo + "-" + pageSize;

        Object cachedRaw = new Object();

        when(baseRedisService.get(redisKey)).thenReturn(cachedRaw);

        when(redisObjectMapper.convertValue(eq(cachedRaw), any(TypeReference.class)))
                .thenReturn(pageResponse);

        PageResponse<List<UserResponse>> result = userService.searchUser(pageNo, pageSize, key);

        assertNotNull(result);
        assertEquals(pageResponse, result);

        verify(baseRedisService).get(redisKey);
        verify(redisObjectMapper).convertValue(eq(cachedRaw), any(TypeReference.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void searchUser_cacheMiss_withKey_success() {
        userResponse = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john.doe123@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar.jpg")
                .bio("hello")
                .status("OFFLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();

        pageResponse = PageResponse.<List<UserResponse>>builder()
                .pageNo(1)
                .pageSize(10)
                .items(List.of(userResponse)) // Sử dụng userResponse thay vì user1
                .totalPages(1)
                .totalElements(1)
                .build();

        int pageNo = 1;
        int pageSize = 10;
        String key = "john";
        String redisKey = "users:search:" + key + ":" + pageNo + "-" + pageSize;
        Page<User> pageMock = new PageImpl<>(List.of(user));
        when(baseRedisService.get(redisKey)).thenReturn(null);
        when(userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(any(), eq(key), eq(AccountStatus.ACTIVE)))
                .thenReturn(pageMock);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        PageResponse<List<UserResponse>> result = userService.searchUser(pageNo, pageSize, key);
        log.info("result: {}", result.getItems());
        assertNotNull(result);
        assertEquals(pageResponse, result);

        verify(baseRedisService).set(eq(redisKey), any(PageResponse.class));
        verify(baseRedisService).setTimeToLive(redisKey, 60);
    }

    @Test
    void searchUser_cacheMiss_notKey_success() {
        // GIVEN
        userResponse = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john.doe123@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar.jpg")
                .bio("hello")
                .status("OFFLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();

        pageResponse = PageResponse.<List<UserResponse>>builder()
                .pageNo(1)
                .pageSize(10)
                .items(List.of(userResponse))
                .totalPages(1)
                .totalElements(1)
                .build();

        int pageNo = 1;
        int pageSize = 10;
        String key = "";
        String redisKey = "users:search::" + pageNo + "-" + pageSize;
        Page<User> pageMock = new PageImpl<>(List.of(user), PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "username")), 1);

        when(baseRedisService.get(redisKey)).thenReturn(null);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageMock);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // WHEN
        PageResponse<List<UserResponse>> result = userService.searchUser(pageNo, pageSize, key);
        log.info("result: {}", result.getItems());

        // THEN
        assertNotNull(result);
        assertEquals(pageResponse, result);

        verify(baseRedisService).set(eq(redisKey), any(PageResponse.class));
        verify(baseRedisService).setTimeToLive(redisKey, 60);
        verify(userRepository).findAll(any(Pageable.class));
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void deleteUser_success() {
        long userId = 1L;
        user.setAccountStatus(AccountStatus.INACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deleteUser(userId);

        assertEquals(AccountStatus.INACTIVE, user.getAccountStatus());

        // Verify interactions
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(baseRedisService).delete(USER_KEY + userId);
    }

    @Test
    void deleteUser_whenUserNotFound_throwsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(baseRedisService, never()).delete(anyString());
    }

    @Test
    void updateUserOnline_success() {
        long userId = 1L;
        user.setStatus(UserStatus.ONLINE);
        user.setLastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        doNothing().when(webSocketService).setOnlineStatus(userId);

        userService.setUserOnline(userId);

        assertEquals(UserStatus.ONLINE, user.getStatus());

    }

    @Test
    void updateUserOnline_whenUserNotFound_throwsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> userService.setUserOnline(userId));

        assertEquals("not found user", exception.getMessage());
    }

    @Test
    void updateUserOffline_success() {
        long userId = 1L;
        user.setStatus(UserStatus.OFFLINE);
        user.setLastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        doNothing().when(webSocketService).setOfflineStatus(userId);

        userService.setUserOffline(userId);

        assertEquals(UserStatus.OFFLINE, user.getStatus());
    }

    @Test
    void updateUserOffline_whenUserNotFound_throwsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> userService.setUserOffline(userId));

        assertEquals("not found user", exception.getMessage());
    }

    @Test
    void getUserOnline_success() {
        int pageNo = 1;
        int pageSize = 10;
        User user3 = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar1.jpg")
                .bio("Hello, I'm John!")
                .status(UserStatus.ONLINE)
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();
       User user4 = User.builder()
               .id(2L)
               .username("jane_doe")
               .email("jane.doe@example.com")
               .fullName("Jane Doe")
               .avatarUrl("http://example.com/avatar2.jpg")
               .bio("Hello, I'm Jane!")
               .status(UserStatus.ONLINE)
               .role(UserRole.USER)
               .lastActive(LocalDateTime.of(2025, 7, 3, 17, 40, 0))
               .build();
        user2.setStatus("ONLINE");
        Page<User> pageMock = new PageImpl<>(List.of(user3, user4),
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "lastActive")),
                2);

        when(userRepository.findByStatus(eq(UserStatus.ONLINE), any(Pageable.class))).thenReturn(pageMock);
        when(userMapper.toUserResponse(user3)).thenReturn(user1);
        when(userMapper.toUserResponse(user4)).thenReturn(user2);

        PageResponse<List<UserResponse>> result = userService.getUsersOnline(pageNo, pageSize);

        assertNotNull(result);
        assertEquals(pageResponse, result);
    }

    @Test
    void getAllUsers_withKey_success() {
        int pageNo = 1;
        int pageSize = 10;
        String key = "john";
        String[] sorts = {"username:asc"};

        User user1 = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar1.jpg")
                .bio("Hello, I'm John!")
                .status(UserStatus.ONLINE)
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();
        UserResponse userRes = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar1.jpg")
                .bio("Hello, I'm John!")
                .status("ONLINE")
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();

        Page<User> pageMock = new PageImpl<>(
                List.of(user1),
                PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.ASC, "username")),
                1  // totalElements
        );

        when(userRepository.getUserWithKeyword(eq(key), any(Pageable.class)))
                .thenReturn(pageMock);
        when(userMapper.toUserResponse(user1)).thenReturn(userRes);

        PageResponse<List<UserResponse>> result = userService.getAllUsers(pageNo, pageSize, key, sorts);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(List.of(userRes), result.getItems());

        verify(userRepository).getUserWithKeyword(eq(key), any(Pageable.class));
        verify(userMapper).toUserResponse(user1);
    }

    @Test
    void getAllUsers_withoutKey_success() {
        int pageNo = 1;
        int pageSize = 10;
        String key = "";
        String[] sorts = {"email:desc"};
        User user3 = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar1.jpg")
                .bio("Hello, I'm John!")
                .status(UserStatus.ONLINE)
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 42, 0))
                .build();
        User user4 = User.builder()
                .id(2L)
                .username("jane_doe")
                .email("jane.doe@example.com")
                .fullName("Jane Doe")
                .avatarUrl("http://example.com/avatar2.jpg")
                .bio("Hello, I'm Jane!")
                .status(UserStatus.ONLINE)
                .role(UserRole.USER)
                .lastActive(LocalDateTime.of(2025, 7, 3, 17, 40, 0))
                .build();
        Page<User> pageMock = new PageImpl<>(List.of(user3, user4),
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "email")),
                2);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageMock);
        when(userMapper.toUserResponse(user3)).thenReturn(user1);
        when(userMapper.toUserResponse(user4)).thenReturn(user2);
        PageResponse<List<UserResponse>> result = userService.getAllUsers(pageNo, pageSize, key, sorts);


        assertNotNull(result);
        assertEquals(pageResponse, result);
    }

    @Test
    void updateStatus_activeToInactive_success() {
        long userId = 1L;
        user.setAccountStatus(AccountStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateStatus(userId);

        assertEquals(AccountStatus.INACTIVE, user.getAccountStatus());
        verify(userRepository).save(user);
        verify(baseRedisService).delete("user:" + userId);
    }

    @Test
    void updateStatus_inactiveToActive_success() {
        long userId = 1L;
        user.setAccountStatus(AccountStatus.INACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateStatus(userId);

        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        verify(userRepository).save(user);
        verify(baseRedisService).delete("user:" + userId);
    }

    @Test
    void updateStatus_notFoundUser_throwsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateStatus(userId));

        verify(userRepository, never()).save(any());
        verify(baseRedisService, never()).delete(anyString());
    }

    @Test
    void updateStatus_Locked_failed() {
        long userId = 1L;
        user.setAccountStatus(AccountStatus.LOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var exception = assertThrows(IllegalStateException.class, () -> userService.updateStatus(userId));

        assertEquals("Không thể thay đổi trạng thái của tài khoản bị LOCKED!", exception.getMessage());
    }
    @Test
    void updateLastActiveTime_success() {
        long userId = 1L;
        user.setStatus(UserStatus.OFFLINE);
        user.setLastActive(LocalDateTime.of(2025, 7, 3, 12, 0));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateLastActiveTime(userId);

        assertEquals(UserStatus.ONLINE, user.getStatus());
        assertTrue(user.getLastActive().isBefore(LocalDateTime.now().plusSeconds(5)));

        verify(userRepository).save(user);
    }

    @Test
    void updateLastActive_notFoundUser_throwsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateLastActiveTime(userId));

        assertEquals("Không tìm thấy người dùng với ID: "+ userId, exception.getMessage());
    }

    @Test
    void loadUserByUsername_found_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");
        assertNotNull(userDetails);
    }

    @Test
    void loadUserByUsername_notFound_throwException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("test@example.com"));
    }

    @Test
    void autoMarkUserOffline_shouldMarkUsersOffline_whenInactive() {
        // GIVEN
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setStatus(UserStatus.ONLINE);
        user1.setLastActive(LocalDateTime.now().minusMinutes(10)); // Trước 5 phút

        List<User> onlineUsers = List.of(user1);

        when(userRepository.findByStatusAndLastActiveBefore(
                eq(UserStatus.ONLINE), any(LocalDateTime.class)
        )).thenReturn(onlineUsers);

        // WHEN
        userService.autoMarkUserOffline();

        // THEN
        assertEquals(UserStatus.OFFLINE, user1.getStatus());
        verify(userRepository).saveAll(onlineUsers);
        verify(webSocketService).setOfflineStatus(user1.getId());
    }

    @Test
    void autoMarkUserOffline_shouldDoNothing_whenNoUsersFound() {
        // GIVEN
        when(userRepository.findByStatusAndLastActiveBefore(
                eq(UserStatus.ONLINE), any(LocalDateTime.class)
        )).thenReturn(Collections.emptyList());

        // WHEN
        userService.autoMarkUserOffline();

        // THEN
        verify(userRepository, never()).saveAll(any());
        verify(webSocketService, never()).setOfflineStatus(anyLong());
    }
}
