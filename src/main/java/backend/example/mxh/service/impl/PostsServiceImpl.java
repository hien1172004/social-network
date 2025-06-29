package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.ImageResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.entity.PostImage;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.PostsMapper;
import backend.example.mxh.repository.PostImageRepository;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.BaseRedisService;
import backend.example.mxh.service.PostsService;
import backend.example.mxh.service.UploadImageFile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;
    private final UploadImageFile cloudinary;
    private final PostsMapper postsMapper;
    private final PostImageRepository postImageRepository;
    private static final  String POST_KEY = "post:";
    private final BaseRedisService<String, String, PostsResponse> baseRedisService;
    private final BaseRedisService<String, String, PageResponse<List<PostsResponse>>> baseRedisServicePage;

    @Override
    @Transactional
    public long createPost(PostsDTO postsDTO) {
        Posts posts = postsMapper.toPosts(postsDTO);
        postsRepository.save(posts);
        // Gán quan hệ ngược cho PostImage
        if (posts.getPostImage() != null) {
            for (PostImage img : posts.getPostImage()) {
                img.setPosts(posts); // Cực kỳ quan trọng!
            }
        }
        log.info(posts.toString());

        // Xóa cache danh sách posts của user
        String redisKeyPrefix = POST_KEY + "user:" + posts.getUser().getId();
        baseRedisServicePage.deleteByPrefix(redisKeyPrefix);
        log.info("Cleared cache for user posts with prefix: {}", redisKeyPrefix);
        return posts.getId();
    }

    @Transactional
    @Override
    public void updatePost(long id, PostsDTO postsDTO) throws IOException {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        postsMapper.updatePosts(posts, postsDTO);
        // Cập nhật ảnh nếu có ảnh mới
        List<ImageDTO> newImageDTOs = postsDTO.getPostImage();
        if (newImageDTOs != null && !newImageDTOs.isEmpty()) {

            // Xóa ảnh cũ trên Cloudinary và xóa khỏi danh sách hiện tại
            Set<PostImage> oldImages = posts.getPostImage();
            if (oldImages != null && !oldImages.isEmpty()) {
                for (PostImage oldImage : oldImages) {
                    try {
                        cloudinary.deleteImage(oldImage.getPublicId());
                    } catch (Exception e) {
                        log.warn("Failed to delete image from Cloudinary: {}", oldImage.getPublicId());
                    }
                }
                // orphanRemoval = true sẽ tự xóa khỏi DB khi clear list
                oldImages.clear();
            }

            // Tạo danh sách ảnh mới và gán post cho từng ảnh
            List<PostImage> newImages = newImageDTOs.stream()
                    .map(dto -> PostImage.builder()
                            .imageUrl(dto.getImageUrl())
                            .publicId(dto.getPublicId())
                            .posts(posts) // Gán lại post
                            .build())
                    .collect(Collectors.toList());

            posts.getPostImage().addAll(newImages);
        }


        postsRepository.save(posts);
        log.info("Updated post id={} with new content and images", id);

        postsRepository.save(posts);

        // Cập nhật cache
        String redisKey = POST_KEY + id;
        PostsResponse updatedResponse = postsMapper.toPostsResponse(posts);
        baseRedisService.set(redisKey, updatedResponse);
        baseRedisService.setTimeToLive(redisKey, 3600);

        log.info("Updated post id={} and cache", id);

        // Xóa cache danh sách posts của user
        String redisKeyPrefix = POST_KEY + "user:" + posts.getUser().getId();
        baseRedisServicePage.deleteByPrefix(redisKeyPrefix);
        log.info("Cleared cache for user posts with prefix: {}", redisKeyPrefix);
    }


    @Override
    @Transactional
    public void deletePost(long id) throws IOException {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (posts.getPostImage() != null && !posts.getPostImage().isEmpty()) {

            for (PostImage oldImage : posts.getPostImage()) {
                cloudinary.deleteImage(oldImage.getPublicId()); // nếu có dùng Cloudinary
            }
        }
        postsRepository.delete(posts);

        // Xóa cache
        String redisKey = POST_KEY + id;
        baseRedisService.delete(redisKey);
        log.info("Deleted post and cache for id: {}", id);

        // Xóa cache danh sách posts của user
        String redisKeyPrefix = POST_KEY + "user:" + posts.getUser().getId();
        baseRedisServicePage.deleteByPrefix(redisKeyPrefix);
        log.info("Cleared cache for user posts with prefix: {}", redisKeyPrefix);
    }

    @Override
    @Transactional
    public PostsResponse getPostById(long id) {
        String redisKey = POST_KEY + id;
        PostsResponse cached = baseRedisService.get(redisKey);
        if (cached != null) return cached;
        Posts posts = postsRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        PostsResponse response = postsMapper.toPostsResponse(posts);

        // Lưu vào Redis để cache
        baseRedisService.set(redisKey, response);
        baseRedisService.setTimeToLive(redisKey, 3600); // TTL = 1 giờ

        log.info("Cache miss. Saved post {} to Redis", id);
        return response;
    }

    @Override
    public PageResponse<List<PostsResponse>> getAllPosts(int pageNo, int pageSize) {
        int page = Math.max(0, pageNo - 1);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createdAt");
        Page<Posts> posts = postsRepository.findAll(pageable);
        return PageResponse.<List<PostsResponse>>builder()
                .pageNo(pageNo)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .pageSize(pageSize)
                .items(posts.stream().map(postsMapper::toPostsResponse).toList())
                .build();
    }

    @Override
    public PageResponse<List<PostsResponse>> getPostsByUserId(int pageNo, int pageSize, long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Tạo key Redis duy nhất dựa trên các tham số
        String redisKey = POST_KEY + "user:" + userId + ":page:" + pageNo + ":size:" + pageSize +
                (startDate != null && endDate != null ? ":start:" + startDate.toString() + ":end:" + endDate.toString() : "");

        // Kiểm tra cache
        PageResponse<List<PostsResponse>> cached = baseRedisServicePage.get(redisKey);
        if (cached != null) {
            log.info("Cache hit for posts of userId: {}, page: {}, size: {}", userId, pageNo, pageSize);
            return cached;
        }
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createdAt");
        Page<Posts> posts;
        if (startDate != null && endDate != null) {
            posts = postsRepository.getPostsOfUserInTime(userId, startDate, endDate, pageable);
        } else {

            posts = postsRepository.findByUser_Id(userId, pageable);
        }

        PageResponse<List<PostsResponse>> response = PageResponse.<List<PostsResponse>>builder()
                .pageNo(pageNo)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .pageSize(pageSize)
                .items(posts.stream().map(postsMapper::toPostsResponse).toList())
                .build();
        // cache du lieu
        baseRedisServicePage.set(redisKey, response);
        baseRedisService.setTimeToLive(redisKey, 3600); // TTL 1 giờ

        return response;
    }

}
