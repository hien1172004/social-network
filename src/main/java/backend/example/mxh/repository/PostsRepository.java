package backend.example.mxh.repository;

import backend.example.mxh.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    List<Posts> findByUser_IdOrderByCreatedAtDesc(Long userId);
}