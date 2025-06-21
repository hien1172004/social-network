package backend.example.mxh.repository;

import backend.example.mxh.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPosts_IdOrderByCreatedAtDesc(Long postsId);
}