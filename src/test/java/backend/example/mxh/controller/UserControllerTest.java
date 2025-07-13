package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.EmailAlreadyExistsException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.service.JwtService;
import backend.example.mxh.service.UserService;
import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Slf4j
@TestPropertySource("/test.properties")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    private AddUserDTO addUserDTO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UpdateUserDTO updateUserDTO;

    private ImageDTO imageDTO;

    private UserResponse userResponse;

    private PageResponse<List<UserResponse>> pageResponse;
    @BeforeEach
    void initData() {
        addUserDTO = AddUserDTO.builder()
                .username("john")
                .fullName("John Doe")
                .email("john.doe123@example.com")
                .phoneNumber("0342610712")
                .password("admin123")
                .bio("12dfndvndskl")
                .build();
        updateUserDTO = UpdateUserDTO.builder()
                .email("john.doe123@example.com")
                .phoneNumber("0342610713")
                .fullName("John Doe143")
                .username("john week")
                .bio("12dfndvndskl")
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
        UserResponse user1 = UserResponse.builder()
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

        UserResponse user2 = UserResponse.builder()
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
    @WithMockUser(roles = {"ADMIN"})
    void createUser_validRequest_shouldReturn201() throws Exception {
        //GIVEN
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(addUserDTO);

        when(userService.addUser(ArgumentMatchers.any(AddUserDTO.class))).thenReturn(2L);
        //WHEN-THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data").value(2L));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_invalidUsernameTooShort_shouldReturn400() throws Exception {
        addUserDTO.setUsername("ab");
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(addUserDTO);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_usernameTooLong_shouldReturn400() throws Exception {
        addUserDTO.setUsername("a".repeat(51));

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_invalidEmailExist_shouldReturn400() throws Exception {
        addUserDTO.setEmail("abcd@gmail.com");
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(addUserDTO);

        when(userService.addUser(ArgumentMatchers.any()))
                .thenThrow(new EmailAlreadyExistsException("Email đã tồn tại"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email đã tồn tại"))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_fullNameMissing_shouldReturn400() throws Exception {
        addUserDTO.setFullName("");

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Full name is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_phoneNumberInvalid_shouldReturn400() throws Exception {
        addUserDTO.setPhoneNumber("1234abc");

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Phone number must be 10-11 digits"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_passwordInvalid_shouldReturn400() throws Exception {
        addUserDTO.setPassword("12345");  // Quá ngắn, thiếu chữ

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Password must be 6-20 characters, include at least 1 letter and 1 number"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_emailInvalid_shouldReturn400() throws Exception {
        addUserDTO.setEmail("not-an-email");

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email format is invalid"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_bioTooLong_shouldReturn400() throws Exception {
        addUserDTO.setBio("a".repeat(501));

        String content = objectMapper.writeValueAsString(addUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bio must not exceed 500 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_validRequest_shouldReturn200() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/1")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Cập nhật người dùng thành công"));
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_invalidUsernameTooShort_shouldReturn400() throws Exception {
        updateUserDTO.setUsername("ab");  // Invalid
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_invalidUsernameBlank_shouldReturn400() throws Exception {
        updateUserDTO.setUsername(" ");  // Invalid
        log.info("Username: [{}]", updateUserDTO.getUsername());
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Username is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_invalidFullNameTooLong_shouldReturn400() throws Exception {
        updateUserDTO.setFullName("A".repeat(101));  // Invalid
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Full name must not exceed 100 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_invalidPhoneNumber_shouldReturn400() throws Exception {
        updateUserDTO.setPhoneNumber("1234");  // Invalid
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Phone number must be 10-11 digits"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_invalidEmailFormat_shouldReturn400() throws Exception {
        updateUserDTO.setUsername("validUsername");
        updateUserDTO.setEmail("invalid-email");  // Invalid
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email format is invalid"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_invalidBioTooLong_shouldReturn400() throws Exception {
        updateUserDTO.setUsername("validUsername");
        updateUserDTO.setBio("A".repeat(501));  // Invalid
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bio must not exceed 500 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(username = "1")
    void updateUser_accessDenied_shouldReturn403() throws Exception {
        // Tạo instance của User entity
        User user = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .password("password")
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        // Thiết lập SecurityContext với User làm principal
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String content = objectMapper.writeValueAsString(updateUserDTO);

        mockMvc.perform(put("/api/v1/users/{id}", 999L)  // ID khác username mock
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateUser_userNotFound_shouldReturn404() throws Exception {
        String content = objectMapper.writeValueAsString(updateUserDTO);

        Mockito.doThrow(new ResourceNotFoundException("not found user"))
                .when(userService).updateUser(ArgumentMatchers.eq(1L), ArgumentMatchers.any());
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("not found user"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "1")
    void updateAvatar_shouldReturn200() throws Exception {
        String content = objectMapper.writeValueAsString(imageDTO);

        mockMvc.perform(put("/api/v1/users/{id}/avatar", 2L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Cập nhật avatar thành công"));
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAvatar_InvalidImageUrlBlank_shouldReturn400() throws Exception {
        imageDTO.setImageUrl(null);
        String content = objectMapper.writeValueAsString(imageDTO);
        mockMvc.perform(put("/api/v1/users/{id}/avatar", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("imageURl is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateAvatar_InvalidPublicIdlBlank_shouldReturn400() throws Exception {
        imageDTO.setPublicId(null);
        String content = objectMapper.writeValueAsString(imageDTO);
        mockMvc.perform(put("/api/v1/users/{id}/avatar", 1L)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("publicId is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }
    @Test
    @WithMockUser
    void getUserById_validId_shouldReturn200() throws Exception {

        when(userService.getDetailUser(anyLong())).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("john_doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.avatarUrl").value("http://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.bio").value("Hello, I'm John!"))
                .andExpect(jsonPath("$.data.status").value("ONLINE"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.lastActive").value("2025-07-03 17:42:00"));
    }

    @Test
    @WithMockUser
    void getUserById_notfound_shouldReturn404() throws Exception {

        when(userService.getDetailUser(999L)).thenThrow(new ResourceNotFoundException("not found user"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("not found user"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getUserById_unauthorized_shouldReturn401() throws Exception {
        long id = 1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void searchUsers_validId_shouldReturn200() throws Exception {
        int pageNo = 1;
        int pageSize = 10;
        String keyword = "doe";
        when(userService.searchUser(pageNo, pageSize, keyword)).thenReturn(pageResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/search")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tìm kiếm người dùng thành công"))
                .andExpect(jsonPath("$.data.pageNo").value(pageNo))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.totalElements").value(pageResponse.getTotalElements()))
                .andExpect(jsonPath("$.data.totalPages").value(pageResponse.getTotalPages()))
                .andExpect(jsonPath("$.data.items[0].id").value(1L))
                .andExpect(jsonPath("$.data.items[0].username").value("john_doe"))
                .andExpect(jsonPath("$.data.items[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.items[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.items[0].avatarUrl").value("http://example.com/avatar1.jpg"))
                .andExpect(jsonPath("$.data.items[0].bio").value("Hello, I'm John!"))
                .andExpect(jsonPath("$.data.items[0].status").value("ONLINE"))
                .andExpect(jsonPath("$.data.items[0].role").value("USER"))
                .andExpect(jsonPath("$.data.items[0].lastActive").value("2025-07-03 17:42:00"))
                .andExpect(jsonPath("$.data.items[1].id").value(2L))
                .andExpect(jsonPath("$.data.items[1].username").value("jane_doe"))
                .andExpect(jsonPath("$.data.items[1].email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.data.items[1].fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.items[1].avatarUrl").value("http://example.com/avatar2.jpg"))
                .andExpect(jsonPath("$.data.items[1].bio").value("Hello, I'm Jane!"))
                .andExpect(jsonPath("$.data.items[1].status").value("OFFLINE"))
                .andExpect(jsonPath("$.data.items[1].role").value("USER"))
                .andExpect(jsonPath("$.data.items[1].lastActive").value("2025-07-03 17:40:00"));

    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_validId_shouldReturn200() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Xoá người dùng thành công"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_notFound_shouldReturn404() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("not found user"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("not found user"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStatus_validId_shouldReturn200() throws Exception {
        Mockito.doNothing().when(userService).updateStatus(1L);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/{id}/account-status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái thành công"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateStatus_notFound_shouldReturn404() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("not found user"))
                .when(userService).updateStatus(999L);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/{id}/account-status", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("not found user"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser
    void getUsersOnline_shouldReturn200() throws Exception {
        when(userService.getUsersOnline(1, 10)).thenReturn(pageResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/online")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy ngươi dung online thanh cong"))
                .andExpect(jsonPath("$.data.pageNo").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    @WithMockUser(roles ={"ADMIN"})
    void getAllUsers_shouldReturn200() throws Exception {
        when(userService.getAllUsers(1, 10, "doe", new String[]{"username:desc"})).thenReturn(pageResponse);

        int pageNo = 1;
        int pageSize = 10;
        String keyword = "doe";
        when(userService.getAllUsers(pageNo, pageSize, keyword)).thenReturn(pageResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/")
                        .param("keyword", keyword)
                        .param("pageNo", "1")
                        .param("sortBy", "username:desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("thanh cong"))
                .andExpect(jsonPath("$.data.pageNo").value(pageNo))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andExpect(jsonPath("$.data.totalElements").value(pageResponse.getTotalElements()))
                .andExpect(jsonPath("$.data.totalPages").value(pageResponse.getTotalPages()))
                .andExpect(jsonPath("$.data.items[0].id").value(1L))
                .andExpect(jsonPath("$.data.items[0].username").value("john_doe"))
                .andExpect(jsonPath("$.data.items[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.items[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.items[0].avatarUrl").value("http://example.com/avatar1.jpg"))
                .andExpect(jsonPath("$.data.items[0].bio").value("Hello, I'm John!"))
                .andExpect(jsonPath("$.data.items[0].status").value("ONLINE"))
                .andExpect(jsonPath("$.data.items[0].role").value("USER"))
                .andExpect(jsonPath("$.data.items[0].lastActive").value("2025-07-03 17:42:00"))
                .andExpect(jsonPath("$.data.items[1].id").value(2L))
                .andExpect(jsonPath("$.data.items[1].username").value("jane_doe"))
                .andExpect(jsonPath("$.data.items[1].email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.data.items[1].fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.items[1].avatarUrl").value("http://example.com/avatar2.jpg"))
                .andExpect(jsonPath("$.data.items[1].bio").value("Hello, I'm Jane!"))
                .andExpect(jsonPath("$.data.items[1].status").value("OFFLINE"))
                .andExpect(jsonPath("$.data.items[1].role").value("USER"))
                .andExpect(jsonPath("$.data.items[1].lastActive").value("2025-07-03 17:40:00"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllUsers_userRole_shouldReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden());
    }

}
