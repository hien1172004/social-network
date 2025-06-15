package backend.example.mxh.repository;

import backend.example.mxh.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPosts_IdOrderByCreatedAtDesc(Long postsId);
}