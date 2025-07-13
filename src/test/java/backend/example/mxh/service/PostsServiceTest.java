package backend.example.mxh.service;

import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.ImageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.entity.PostImage;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.PostsMapper;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.impl.PostsServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {
    @InjectMocks
    private PostsServiceImpl postsService;
    @Mock
    private PostsRepository postsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UploadImageFile cloudinary;

    @Mock
    private PostsMapper postsMapper;

    @Mock
    private BaseRedisService<String, String, Object> baseRedisService;

    @Mock
    private ObjectMapper redisObjectMapper;

    private PostsDTO postsDTO;

    private static final String POST_KEY = "post:";
    private static final String USER_KEY = "user:";

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
    }


    @Test
    void createPost_WithPostImage_ShouldCreatePost() {
        when(postsMapper.toPosts(any(PostsDTO.class))).thenReturn(posts);
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        long postId = postsService.createPost(postsDTO);

        assertEquals(1L, postId);
        posts.getPostImage().forEach(img -> assertEquals(posts, img.getPosts()));

    }

    @Test
    void createPost_WithPostImageNull_ShouldCreatePost() {
        postsDTO.setPostImage(null);
        posts.setPostImage(null);
        when(postsMapper.toPosts(any(PostsDTO.class))).thenReturn(posts);
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        long postId = postsService.createPost(postsDTO);

        assertEquals(1L, postId);
    }

    @Test
    void updatePost_WithPostImage_ShouldUpdatePost() throws IOException {
        long postsId = 1;
        image1 = ImageDTO.builder()
                .publicId("publicId1 updated")
                .imageUrl("imageUrl1 updated")
                .build();
        image2 = ImageDTO.builder()
                .publicId("publicId2 updated")
                .imageUrl("imageUrl2 updated")
                .build();
        postImage1 = PostImage.builder()
                .publicId("publicId1 updated")
                .imageUrl("imageUrl1 updated")
                .build();
        postImage2 = PostImage.builder()
                .publicId("publicId2 updated")
                .imageUrl("imageUrl2 updated")
                .build();
        postsDTO = PostsDTO.builder()
                .content("updated content")
                .userId(1L)
                .postImage(List.of(image1, image2))
                .build();
        posts = Posts.builder()
                .id(postsId)
                .content("updated content")
                .user(user)
                .postImage(new HashSet<>(Set.of(PostImage.builder()
                        .publicId("old-public-id")
                        .imageUrl("old-url")
                        .build())))
                .build();
        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(posts));
        doNothing().when(postsMapper).updatePosts(posts, postsDTO);
        doNothing().when(cloudinary).deleteImage(anyString());
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.updatePost(postsId, postsDTO);

        assertEquals(1L, postsId);
        assertEquals("updated content", postsDTO.getContent());
        posts.getPostImage().forEach(img -> assertEquals(posts, img.getPosts()));
        assertEquals(2, posts.getPostImage().size());
        assertTrue(posts.getPostImage().stream().anyMatch(img -> img.getPublicId().equals("publicId1 updated")));
        assertTrue(posts.getPostImage().stream().anyMatch(img -> img.getPublicId().equals("publicId2 updated")));

        verify(baseRedisService).deleteByPrefix(anyString());
        verify(postsRepository).findById(anyLong());
        verify(postsRepository).save(any(Posts.class));
    }
    @Test
    void updatePost_WithPostImageNull_ShouldUpdatePost() {
        long postsId = 1L;
        postsDTO.setPostImage(null);
        postsDTO.setContent("updated content");
        posts.setPostImage(new HashSet<>());
        when(postsRepository.findById(postsId)).thenReturn(Optional.of(posts));
        doAnswer(invocation -> {
            Posts postsArg = invocation.getArgument(0);
            PostsDTO dtoArg = invocation.getArgument(1);
            postsArg.setContent(dtoArg.getContent());
            return null;
        }).when(postsMapper).updatePosts(posts, postsDTO);
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.updatePost(postsId, postsDTO);

        assertEquals(posts.getContent(), postsDTO.getContent());
        // Không có ảnh mới, không xóa ảnh cũ
        assertTrue(posts.getPostImage().isEmpty());
    }

    @Test
    void updatePost_WhenDeleteImageFails_ShouldLogWarning() throws IOException {
        long postsId = 1L;
        // Chuẩn bị dữ liệu: có ảnh cũ, có ảnh mới
        postsDTO.setPostImage(List.of(image1));
        postsDTO.setContent("updated content");
        PostImage oldImage = PostImage.builder()
                .publicId("old-public-id")
                .imageUrl("old-url")
                .build();
        posts.setPostImage(new HashSet<>(Set.of(oldImage)));
        when(postsRepository.findById(postsId)).thenReturn(Optional.of(posts));
        doAnswer(invocation -> {
            Posts postsArg = invocation.getArgument(0);
            PostsDTO dtoArg = invocation.getArgument(1);
            postsArg.setContent(dtoArg.getContent());
            return null;
        }).when(postsMapper).updatePosts(posts, postsDTO);
        // Ném exception khi xóa ảnh
        doThrow(new IOException("Cloudinary error")).when(cloudinary).deleteImage(anyString());
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        // Không throw ra ngoài, chỉ log warning
        assertDoesNotThrow(() -> postsService.updatePost(postsId, postsDTO));
        // Có thể verify cloudinary.deleteImage được gọi
        verify(cloudinary, times(1)).deleteImage("old-public-id");
    }
    @Test
    void upadatePost_WithNotPostImage_ShouldUpdatePost()  {
        // GIVEN
        long postsId = 1L;

        // PostsDTO không có postImage
        postsDTO = PostsDTO.builder()
                .content("updated content no images")
                .userId(1L)
                .postImage(List.of()) // có thể null hoặc List.of()
                .build();

        // Entity posts giả lập có sẵn ảnh cũ (cũng được)
        posts = Posts.builder()
                .id(postsId)
                .content("old content")
                .user(user)
                .postImage(new HashSet<>()) // giả sử ban đầu empty
                .build();

        when(postsRepository.findById(postsId)).thenReturn(Optional.of(posts));
        doAnswer(invocation -> {
            Posts postsArg = invocation.getArgument(0);
            PostsDTO dtoArg = invocation.getArgument(1);
            postsArg.setContent(dtoArg.getContent());
            return null;
        }).when(postsMapper).updatePosts(posts, postsDTO);

        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        // WHEN
        postsService.updatePost(postsId, postsDTO);

        // THEN
        assertEquals("updated content no images", posts.getContent());
        assertTrue(posts.getPostImage().isEmpty()); // vẫn không có ảnh

        // Verify gọi các bước quan trọng
        verify(postsMapper, times(1)).updatePosts(posts, postsDTO);
        verify(postsRepository, times(1)).save(posts);
        verify(baseRedisService, times(1)).deleteByPrefix(contains(POST_KEY + USER_KEY + posts.getUser().getId()));
    }

    @Test
    void updatePost_WithOldImagesNull_ShouldUpdatePost() {
        long postsId = 1L;
        postsDTO.setPostImage(List.of(image1));
        posts.setPostImage(null); // oldImages null
        when(postsRepository.findById(postsId)).thenReturn(Optional.of(posts));
        doAnswer(invocation -> {
            Posts postsArg = invocation.getArgument(0);
            PostsDTO dtoArg = invocation.getArgument(1);
            postsArg.setContent(dtoArg.getContent());
            return null;
        }).when(postsMapper).updatePosts(posts, postsDTO);
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.updatePost(postsId, postsDTO);
        // Không có lỗi, không xóa ảnh cũ
        assertEquals(posts.getContent(), postsDTO.getContent());
        assertEquals(image1, postsDTO.getPostImage().get(0));
    }

    @Test
    void updatePost_WithOldImagesEmpty_ShouldUpdatePost() {
        long postsId = 1L;
        postsDTO.setPostImage(List.of(image1));
        posts.setPostImage(new HashSet<>()); // oldImages empty
        when(postsRepository.findById(postsId)).thenReturn(Optional.of(posts));
        doAnswer(invocation -> {
            Posts postsArg = invocation.getArgument(0);
            PostsDTO dtoArg = invocation.getArgument(1);
            postsArg.setContent(dtoArg.getContent());
            return null;
        }).when(postsMapper).updatePosts(posts, postsDTO);
        when(postsRepository.save(any(Posts.class))).thenReturn(posts);
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.updatePost(postsId, postsDTO);
        // Không có lỗi, không xóa ảnh cũ
        assertEquals(posts.getContent(), postsDTO.getContent());
        assertEquals(image1, postsDTO.getPostImage().get(0));
    }

    @Test
    void deletePost_WithPostImage_ShouldDeletePost() throws IOException {

        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(posts));
        doNothing().when(cloudinary).deleteImage(anyString());
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.deletePost(anyLong());

        assertEquals(1L, posts.getId());
        verify(postsRepository, times(1)).findById(anyLong());
        verify(baseRedisService, times(1)).delete(anyString());
        verify(baseRedisService, times(1)).deleteByPrefix(anyString());
    }
    @Test
    void deletePost_NotFound_ThrowException(){

        when(postsRepository.findById(anyLong())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> postsService.deletePost(1L));

        assertEquals("Post not found", exception.getMessage());
    }
    @Test
    void deletePost_WithPostImageEmpty_ShouldDeletePost() throws IOException {
        posts.setPostImage(new HashSet<>()); // empty set
        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(posts));
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.deletePost(posts.getId());

        verify(postsRepository, times(1)).findById(anyLong());
        verify(postsRepository, times(1)).delete(posts);
        verify(baseRedisService, times(1)).delete(anyString());
        verify(baseRedisService, times(1)).deleteByPrefix(anyString());
    }

    @Test
    void deletePost_WithPostImageNull_ShouldDeletePost() throws IOException {
        posts.setPostImage(null); // empty set
        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(posts));
        doNothing().when(baseRedisService).delete(anyString());
        doNothing().when(baseRedisService).deleteByPrefix(anyString());

        postsService.deletePost(posts.getId());

        verify(postsRepository, times(1)).findById(anyLong());
        verify(postsRepository, times(1)).delete(posts);
        verify(baseRedisService, times(1)).delete(anyString());
        verify(baseRedisService, times(1)).deleteByPrefix(anyString());
    }


    @Test
    void getPostById_HitCache_ShouldGetPost() {
        Object cached = new Object();
        when(baseRedisService.get(anyString())).thenReturn(cached);
        when(redisObjectMapper.convertValue(cached, PostsResponse.class)).thenReturn(postsResponse);

        PostsResponse result = postsService.getPostById(anyLong());

        assertEquals(postsResponse, result);
    }

    @Test
    void getPostById_NotFound_ThrowException() {

        when(baseRedisService.get(anyString())).thenReturn(null);
        when(postsRepository.findWithDetailsById(anyLong())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> postsService.getPostById(1L));
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    void getPostById_MissCache_ShouldGetPostImage() {

        long postId = 1L;

        when(baseRedisService.get("post:" + postId)).thenReturn(null);
        when(postsRepository.findWithDetailsById(postId)).thenReturn(Optional.of(posts));
        when(postsMapper.toPostsResponse(posts)).thenReturn(postsResponse);

        PostsResponse result = postsService.getPostById(postId);

        assertEquals(postsResponse, result);

        verify(baseRedisService).set(POST_KEY + postId, postsResponse);
        verify(baseRedisService).setTimeToLive(POST_KEY + postId, 3600L);
    }

    @Test
    void getAllPosts_ShouldGetAllPosts() {
        int pageNo = 1;
        int pageSize = 10;
        Posts post1 = Posts.builder()
                .content("content1")
                .user(user)
                .postImage(Set.of(postImage1))
                .build();
        Posts post2 = Posts.builder()
                .content("content2")
                .user(user)
                .postImage(Set.of(postImage2))
                .build();
        PostsResponse postsResponse1 =PostsResponse.builder()
                .id(1L)
                .content("content1")
                .userId(1L)
                .likeQuantity(0L)
                .commentQuantity(0L)
                .postImage(List.of(imageResponse1))
                .createdAt(LocalDateTime.now())
                .build();
        PostsResponse postsResponse2 =PostsResponse.builder()
                .id(2L)
                .content("content2")
                .userId(1L)
                .likeQuantity(0L)
                .commentQuantity(0L)
                .postImage(List.of(imageResponse2))
                .createdAt(LocalDateTime.now())
                .build();

        Page<Posts> pageable = new PageImpl<>(List.of(post1, post2), PageRequest.of(0, pageSize, Sort.Direction.DESC, "createdAt"), 2);

        PageResponse<Object> pageResponse = PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(List.of(postsResponse1, postsResponse2))
                .totalPages(pageable.getTotalPages())
                .totalElements(pageable.getTotalElements())
                .build();

        when(postsRepository.findAll(any(Pageable.class))).thenReturn(pageable);
        when(postsMapper.toPostsResponse(post1)).thenReturn(postsResponse1);
        when(postsMapper.toPostsResponse(post2)).thenReturn(postsResponse2);

        PageResponse<List<PostsResponse>> result = postsService.getAllPosts(pageNo, pageSize);

        assertEquals(pageResponse, result);
    }

    @Test
    void getPostsByUserId_HitCache_ShouldGetPosts() {
        int pageNo = 1;
        int pageSize = 10;
        long userId = 1L;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        PageResponse<List<PostsResponse>> cachedResponse = PageResponse.<List<PostsResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(1)
                .totalElements(1L)
                .items(List.of(postsResponse))
                .build();

        String redisKey = "post:user:" + userId + ":page:" + pageNo + ":size:" + pageSize;

        when(baseRedisService.get(redisKey)).thenReturn(cachedResponse);
        when(redisObjectMapper.convertValue(any(), any(TypeReference.class)))
                .thenReturn(cachedResponse);

        PageResponse<List<PostsResponse>> result = postsService.getPostsByUserId(
                pageNo, pageSize, userId, startDate, endDate
        );

        assertNotNull(result);
        assertEquals(cachedResponse, result);
        verify(baseRedisService, times(1)).get(redisKey);
        verifyNoMoreInteractions(postsRepository); // Không query DB
    }

    @Test
    void getPostsByUserId_CacheMiss_ShouldQueryDatabaseAndCacheResult() {
        int pageNo = 1;
        int pageSize = 10;
        long userId = 1L;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        String redisKey = "post:user:" + userId + ":page:" + pageNo + ":size:" + pageSize;

        when(baseRedisService.get(redisKey)).thenReturn(null); // Cache miss

        Page<Posts> postsPage = new PageImpl<>(List.of(posts));
        when(postsRepository.findByUser_Id(eq(userId), any(Pageable.class)))
                .thenReturn(postsPage);
        when(postsMapper.toPostsResponse(any(Posts.class)))
                .thenReturn(postsResponse);

        PageResponse<List<PostsResponse>> result = postsService.getPostsByUserId(
                pageNo, pageSize, userId, startDate, endDate
        );

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(postsResponse, result.getItems().get(0));

        verify(baseRedisService).get(redisKey);
        verify(postsRepository).findByUser_Id(eq(userId), any(Pageable.class));
        verify(postsMapper).toPostsResponse(posts);
        verify(baseRedisService).set(eq(redisKey), any());
        verify(baseRedisService).setTimeToLive(redisKey, 3600);
    }
    @Test
    void getPostsByUserId_WithStartAndEndDate_ShouldReturnPageResponseAndCache() {
        // GIVEN
        int pageNo = 1;
        int pageSize = 10;
        long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        String redisKey = "post:user:" + userId + ":page:" + pageNo + ":size:" + pageSize
                + ":start:" + startDate + ":end:" + endDate;

        Posts post1 = Posts.builder()
                .content("content1")
                .user(user)
                .postImage(Set.of(postImage1))
                .build();
        Posts post2 = Posts.builder()
                .content("content2")
                .user(user)
                .postImage(Set.of(postImage2))
                .build();
        PostsResponse postsResponse1 =PostsResponse.builder()
                .id(1L)
                .content("content1")
                .userId(1L)
                .likeQuantity(0L)
                .commentQuantity(0L)
                .postImage(List.of(imageResponse1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        PostsResponse postsResponse2 =PostsResponse.builder()
                .id(2L)
                .content("content2")
                .userId(1L)
                .likeQuantity(0L)
                .commentQuantity(0L)
                .postImage(List.of(imageResponse2))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();


        // Cache MISS
        when(baseRedisService.get(redisKey)).thenReturn(null);

        Page<Posts> postsPage = new PageImpl<>(List.of(post1, post2), PageRequest.of(0, pageSize, Sort.Direction.DESC, "createdAt"), 2); // posts = mock Posts entity
        when(postsRepository.getPostsOfUserInTime(anyLong(), any(), any(), any(Pageable.class)))
                .thenReturn(postsPage);


        when(postsMapper.toPostsResponse(any(Posts.class)))
                .thenReturn(postsResponse1)
                .thenReturn(postsResponse2);

        // WHEN
        PageResponse<List<PostsResponse>> response = postsService.getPostsByUserId(
                pageNo, pageSize, userId, startDate, endDate
        );

        // THEN
        assertNotNull(response);
        assertEquals(pageNo, response.getPageNo());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(2, response.getItems().size());
        assertEquals(postsResponse1, response.getItems().get(0));
        assertEquals(postsResponse2, response.getItems().get(1));

    }

}
