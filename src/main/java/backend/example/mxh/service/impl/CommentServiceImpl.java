package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.CommentDTO;
import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.CommentResponse;
import backend.example.mxh.entity.Comment;
import backend.example.mxh.entity.Posts;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.CommentMapper;
import backend.example.mxh.repository.CommentRepository;
import backend.example.mxh.repository.PostsRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.CommentService;
import backend.example.mxh.service.NotificationService;
import backend.example.mxh.until.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final BaseRedisServiceImpl<String, String, List<CommentResponse>> baseRedisService;
    @Override
    @Transactional
    public Long create(CommentDTO dto) {
        // Validate user và bài viết có tồn tại
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Posts post = postsRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));


        Comment comment = Comment.builder()
                .user(user)
                .posts(post)
                .content(dto.getContent())
                .build();

        commentRepository.save(comment);
        log.info("Create comment {}", comment);

        if (!comment.getUser().getId().equals(comment.getPosts().getUser().getId())) {
            // Gửi thông báo sau khi gửi lời mời
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .senderId(comment.getUser().getId())
                    .receiverId(comment.getPosts().getUser().getId())
                    .type(NotificationType.COMMENT) // Enum
                    .referenceId(comment.getId())
                    .build();
            notificationService.createNotification(notificationDTO);
        }
        baseRedisService.delete("comments:post:" + dto.getPostId());

        return comment.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
        log.info("Delete comment : {}", comment);

       baseRedisService.delete("comments:post:" + comment.getPosts().getId());

    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        String key = "comments:post:" + postId;

        // Đọc cache
        List<CommentResponse> cached = baseRedisService.get(key);
        if (cached != null) {
            log.info("Cache HIT for {}", key);
            return cached;
        }
         // Truy vấn DB
        List<CommentResponse> comments = commentRepository.findByPosts_IdOrderByCreatedAtDesc(postId)
                .stream()
                .map(commentMapper::toResponse)
                .toList();

       baseRedisService.set(key, comments);
       baseRedisService.setTimeToLive(key, 300);
        return comments;
    }

    @Override
    @Transactional
    public void update(Long id, CommentDTO dto) {
        log.info("Updating comment with id={} by userId={}, postId={}", id, dto.getUserId(), dto.getPostId());
        validateCommentData(dto);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentMapper.updateComment(comment, dto);
        commentRepository.save(comment);
        log.info("Update comment {}", comment);
        baseRedisService.delete("comments:post:" + dto.getPostId());

    }

    private void validateCommentData(CommentDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        postsRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }
}
