package backend.example.mxh.security;

import backend.example.mxh.entity.Comment;
import backend.example.mxh.entity.User;
import backend.example.mxh.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("commentSecurity")
public class CommentSecurity {
    private final CommentRepository commentRepository;

    public boolean isOwner(Long commentId, Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        return userId.equals(comment.getUser().getId());
    }
}
