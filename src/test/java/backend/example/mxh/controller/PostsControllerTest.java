package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.ImageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.security.PostsControllerTestConfig;
import backend.example.mxh.entity.PostImage;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.entity.User;
import backend.example.mxh.security.PostSecurity;
import backend.example.mxh.service.JwtService;
import backend.example.mxh.service.PostsService;
import backend.example.mxh.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import backend.example.mxh.DTO.response.PageResponse;

@WebMvcTest(controllers = PostsController.class)
@Slf4j
@TestPropertySource("/test.properties")
@Import(PostsControllerTestConfig.class)
class PostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PostsService postsService;

    @MockBean
    private PostSecurity postSecurity;

    @MockBean
    private PostsRepository postsRepository;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PostsDTO postsDTO;
    private Posts posts;

    private ImageDTO image1;
    private ImageDTO image2;

    private PostImage postImage1;
    private PostImage postImage2;
    private User user;
    private PostsResponse postsResponse;
    private ImageResponse imageResponse1;
    private ImageResponse imageResponse2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        image1 = ImageDTO.builder()
                .publicId("publicId1")
                .imageUrl("imageUrl1")
                .build();
        image2 = ImageDTO.builder()
                .publicId("publicId2")
                .imageUrl("imageUrl2")
                .build();
        postsDTO = PostsDTO.builder()
                .content("content 1")
                .userId(1L)
                .postImage(List.of(image1, image2))
                .build();
        postImage1 = PostImage.builder()
                .publicId("publicId1")
                .imageUrl("imageUrl1")
                .build();
        postImage2 = PostImage.builder()
                .publicId("publicId2")
                .imageUrl("imageUrl2")
                .build();
        posts = Posts.builder()
                .id(1L)
                .content("content 1")
                .user(user) // Giả lập User
                .postImage(Set.of(postImage1, postImage2))
                .build();
        imageResponse1 = ImageResponse.builder()
                .publicId("publicId1")
                .imageUrl("imageUrl1")
                .build();
        imageResponse2 = ImageResponse.builder()
                .publicId("publicId2")
                .imageUrl("imageUrl2")
                .build();
        postsResponse = PostsResponse.builder()
                .id(1L)
                .content("content 1")
                .userId(1L)
                .likeQuantity(0L)
                .commentQuantity(0L)
                .postImage(List.of(imageResponse1, imageResponse2))
                .createdAt(LocalDateTime.of(2025, 7, 5, 14, 0, 0))
                .build();

        // Setup default PostSecurity mock behavior
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void createPost_success() throws Exception {
        String content = objectMapper.writeValueAsString(postsDTO);

        when(postsService.createPost(any(PostsDTO.class))).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tạo bài viết thành công"))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @WithMockUser
    void createPost_ContentBlank_fail() throws Exception {
        postsDTO.setContent("");
        String content = objectMapper.writeValueAsString(postsDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Content is required"));
    }

    @Test
    @WithMockUser
    void createImage_ContentTooLong_fail() throws Exception {
        postsDTO.setContent("a".repeat(1001));
        String content = objectMapper.writeValueAsString(postsDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Content must not exceed 1000 characters"));
    }

    @Test
    @WithMockUser
    void createPosts_UserIdNull_fail() throws Exception {
        postsDTO.setUserId(null);
        String content = objectMapper.writeValueAsString(postsDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("User ID is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePost_success_asAdmin() throws Exception {
        postsDTO = PostsDTO.builder()
                .content("updated content")
                .userId(1L)
                .postImage(List.of(image1, image2))
                .build();

        String content = objectMapper.writeValueAsString(postsDTO);

        // Bổ sung mock quan trọng:
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);
        doNothing().when(postsService).updatePost(anyLong(), any(PostsDTO.class));
        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Cập nhật bài viết thành công"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePost_contentBlank_fail() throws Exception {
        postsDTO.setContent("");
        String content = objectMapper.writeValueAsString(postsDTO);

        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Content is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePost_contentTooLong_fail() throws Exception {
        postsDTO.setContent("a".repeat(1001));
        String content = objectMapper.writeValueAsString(postsDTO);

        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Content must not exceed 1000 characters"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePost_userIdNull_fail() throws Exception {
        postsDTO.setUserId(null);
        String content = objectMapper.writeValueAsString(postsDTO);

        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("User ID is required"))
                .andExpect(jsonPath("$.error").value("Payload Invalid"));
    }

    @Test
    @WithMockUser
    void updatePost_success_asOwner() throws Exception {
        postsDTO = PostsDTO.builder()
                .content("updated content")
                .userId(1L)
                .postImage(List.of(image1, image2))
                .build();

        String content = objectMapper.writeValueAsString(postsDTO);

        // Mock the PostSecurity to return true for ownership check
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);
        doNothing().when(postsService).updatePost(anyLong(), any(PostsDTO.class));
        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Cập nhật bài viết thành công"));
    }

    @Test
    @WithMockUser
    void updatePost_unauthorized() throws Exception {
        postsDTO = PostsDTO.builder()
                .content("updated content")
                .userId(1L)
                .postImage(List.of(image1, image2))
                .build();

        String content = objectMapper.writeValueAsString(postsDTO);

        // Mock the PostSecurity to return false for ownership check
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(false);
        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePost_success_asAdmin() throws Exception {
        doNothing().when(postsService).deletePost(anyLong());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("Xóa bài viết thành công"));
    }

    @Test
    @WithMockUser
    void deletePost_success_asOwner() throws Exception {
        // Mock the PostSecurity to return true for ownership check
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(true);
        doNothing().when(postsService).deletePost(anyLong());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("Xóa bài viết thành công"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePost_unauthorized() throws Exception {
        // Mock the PostSecurity to return false for ownership check
        when(postSecurity.isOwner(anyLong(), any())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/posts/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getPostById_success_asUser() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());

        when(postsService.getPostById(anyLong())).thenReturn(postsResponse);

        String dataJson = objectMapper.writeValueAsString(postsResponse);
        String expectedResponseJson = """
                {
                  "code": 200,
                  "message": "Lấy bài viết thành công",
                  "data": REPLACE
                }
                """.replace("REPLACE", dataJson);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseJson));
    }

    @Test
    @WithMockUser
    void getPostsByUser_success() throws Exception {
        // Giả lập dữ liệu trả về
        List<PostsResponse> postsList = List.of(postsResponse); // postsResponse đã có sẵn từ setUp()
        PageResponse<List<PostsResponse>> pageResponse = new PageResponse<>();
        pageResponse.setItems(postsList);
        pageResponse.setPageNo(1);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(postsService.getPostsByUserId(1, 10, 1L, null, null)).thenReturn(pageResponse);

        objectMapper.registerModule(new JavaTimeModule());
        String dataJson = objectMapper.writeValueAsString(pageResponse);
        String expectedResponseJson = """
                {
                  "code": 200,
                  "message": "Lấy bài viết theo user thành công",
                  "data": REPLACE
                }
                """.replace("REPLACE", dataJson);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/user/{userId}", 1L)
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseJson));
    }

    @Test
    @WithMockUser
    void getPostById_notFound() throws Exception {
        when(postsService.getPostById(anyLong())).thenThrow(new ResourceNotFoundException("not found post"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("not found post"));
    }

    @Test
    @WithMockUser
    void getPostsByUser_withDateFilter_success() throws Exception {
        LocalDateTime startDate = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 7, 31, 23, 59);

        List<PostsResponse> postsList = List.of(postsResponse);
        PageResponse<List<PostsResponse>> pageResponse = new PageResponse<>();
        pageResponse.setItems(postsList);
        pageResponse.setPageNo(1);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(postsService.getPostsByUserId(1, 10, 1L, startDate, endDate)).thenReturn(pageResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/user/{userId}", 1L)
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("startDate", "2025-07-01T00:00:00")
                        .param("endDate", "2025-07-31T23:59:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy bài viết theo user thành công"))
                .andExpect(jsonPath("$.data.items[0].id").value(postsResponse.getId()));
    }

    @Test
    @WithMockUser
    void getAllPosts_success() throws Exception {
        // Chuẩn bị dữ liệu giả lập
        List<PostsResponse> postsList = List.of(postsResponse); // postsResponse đã có sẵn từ setUp()
        PageResponse<List<PostsResponse>> pageResponse = new PageResponse<>();
        pageResponse.setItems(postsList);
        pageResponse.setPageNo(1);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JavaTimeModule());
        String pageData = objectMapper.writeValueAsString(pageResponse);

        String expectedJson = """
                {
                  "code": 200,
                  "message": "Lấy danh sách bài viết thành công",
                  "data": REPLACE
                }
                """.replace("REPLACE", pageData);
        when(postsService.getAllPosts(1, 10)).thenReturn(pageResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}
