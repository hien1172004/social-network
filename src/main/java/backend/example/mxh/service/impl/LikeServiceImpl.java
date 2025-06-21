package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.LikeDTO;
import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.LikeUserResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.Like;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.LikeMapper;
import backend.example.mxh.mapper.PostsMapper;
import backend.example.mxh.repository.LikeRepository;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.LikeService;
import backend.example.mxh.service.NotificationService;
import backend.example.mxh.service.WebSocketService;
import backend.example.mxh.until.NotificationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
@Slf4j
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;
    private final LikeMapper likeMapper;
    private final NotificationService notificationService;
    @Override
    @Transactional
    public void toggleLike(LikeDTO likeDTO) {
        // Kiểm tra user & post tồn tại
        User user = userRepository.findById(likeDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Posts post = postsRepository.findById(likeDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Optional<Like> existing = likeRepository.findByUser_IdAndPosts_Id(likeDTO.getUserId(), likeDTO.getPostId());
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            log.info("Like deleted");
        }
        else {
            Like like = new Like();
            like.setUser(user);
            like.setPosts(post);
            likeRepository.save(like);
            log.info("Like created");

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .senderId(likeDTO.getUserId())
                    .receiverId(post.getUser().getId())
                    .type(NotificationType.LIKE)
                    .build();
            notificationService.createNotification(notificationDTO);
        }
    }

    @Override
    public long countLikes(Long postId) {
        return likeRepository.countByPosts_Id(postId);
    }

    @Override
    public PageResponse<List<LikeUserResponse>> getUserLikePosts(int pageNo, int pageSize, Long postId) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Like> likes = likeRepository.findByPosts_Id(postId, pageable);
        return PageResponse.<List<LikeUserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(likes.getTotalPages())
                .totalElements(likes.getTotalElements())
                .items(likes.stream().map(likeMapper::toLikeUserResponse).toList())
                .build();
    }
}
