package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.CommentDTO;
import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.CommentResponse;
import backend.example.mxh.entity.Comment;
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

    @Override
    public Long create(CommentDTO dto) {
        // Validate user và bài viết có tồn tại
        validateCommentData(dto);
        Comment comment = commentMapper.toComment(dto);
        commentRepository.save(comment);
        log.info("Create comment {}", comment);


        // Gửi thông báo sau khi gửi lời mời
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .senderId(comment.getUser().getId())
                .receiverId(comment.getPosts().getUser().getId())
                .type(NotificationType.COMMENT.name()) // Enum
                .build();
        if (!comment.getUser().getId().equals(comment.getPosts().getUser().getId())) {
            notificationService.createNotification(notificationDTO);
        }
        return comment.getId();
    }

    @Override
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
        log.info("Delete comment : {}", comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPosts_IdOrderByCreatedAtDesc(postId)
                .stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    public void update(Long id, CommentDTO dto) {
        log.info("Updating comment with id={} by userId={}, postId={}", id, dto.getUserId(), dto.getPostId());
        validateCommentData(dto);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentMapper.updateComment(comment, dto);
        commentRepository.save(comment);
        log.info("Update comment {}", comment);
    }

    private void validateCommentData(CommentDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        postsRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }
}
