package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.entity.PostImage;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.PostsMapper;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.PostsService;
import backend.example.mxh.service.UploadImageFile;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;
    private final UploadImageFile cloudinary;
    private final PostsMapper postsMapper;

    @Override
    public long createPost(PostsDTO postsDTO) {
        Posts posts = postsMapper.toPosts(postsDTO);
        postsRepository.save(posts);
        log.info(posts.toString());
        return posts.getId();
    }

    @Transactional
    @Override
    public void updatePost(long id, PostsDTO postsDTO) throws IOException {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        postsMapper.updatePosts(posts, postsDTO);
        // Cập nhật ảnh nếu có ảnh mới
        if (postsDTO.getPostImage() != null && !postsDTO.getPostImage().isEmpty()) {
            // Xóa ảnh cũ (nếu dùng Cloudinary thì cần xóa ở Cloud trước)
            if (posts.getPostImage() != null) {
                for (PostImage oldImage : posts.getPostImage()) {
                    cloudinary.deleteImage(oldImage.getPublicId()); // nếu có dùng Cloudinary
                }
                posts.getPostImage().clear(); // xóa trong list
            }

            // Thêm danh sách ảnh mới
            List<PostImage> newImages = postsDTO.getPostImage().stream().map(dto ->
                    PostImage.builder().
                            imageUrl(dto.getImageUrl())
                            .publicId(dto.getPublicId()).
                            posts(posts)
                            .build())
                    .collect(Collectors.toList());

            posts.setPostImage(newImages);
        }

        postsRepository.save(posts); // Lưu lại bài viết

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
    public PostsResponse getPostById(long id) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return postsMapper.toPostsResponse(posts);
    }

    @Override
    public List<PostsResponse> getAllPosts() {
        List<Posts> posts = postsRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.stream().map(postsMapper::toPostsResponse).toList();
    }

    @Override
    public List<PostsResponse> getPostsByUserId(long userId) {
        List<Posts> posts = postsRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return posts.stream().map(postsMapper::toPostsResponse).toList();
    }
}
