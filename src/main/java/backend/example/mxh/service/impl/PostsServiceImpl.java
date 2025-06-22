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
        return posts.getId();
    }

    @Transactional
    @Override
    public void updatePost(long id, PostsDTO postsDTO) throws IOException {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        postsMapper.updatePosts(posts, postsDTO);
        // Cập nhật ảnh nếu có ảnh mới
        // Nếu có ảnh mới cần cập nhật
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
}

@Override
@Transactional
public PostsResponse getPostById(long id) {
    Posts posts = postsRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    return postsMapper.toPostsResponse(posts);
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
public PageResponse<List<PostsResponse>> getPostsByUserId(int pageNo, int pageSize, long userId) {
    int page = 0;
    if (pageNo > 0) {
        page = pageNo - 1;
    }
    Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createdAt");
    Page<Posts> posts = postsRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
    return PageResponse.<List<PostsResponse>>builder()
            .pageNo(pageNo)
            .totalElements(posts.getTotalElements())
            .totalPages(posts.getTotalPages())
            .pageSize(pageSize)
            .items(posts.stream().map(postsMapper::toPostsResponse).toList())
            .build();
}
}
