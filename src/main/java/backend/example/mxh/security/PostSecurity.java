package backend.example.mxh.security;

import backend.example.mxh.entity.Posts;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("postSecurity")
@RequiredArgsConstructor
public class PostSecurity {
    private final PostsRepository postsRepository;

    public boolean isOwner(Long postId, Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        Posts post = postsRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return userId.equals(post.getUser().getId());
    }

}
