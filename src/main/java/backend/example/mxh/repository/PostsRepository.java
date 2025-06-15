package backend.example.mxh.repository;


import backend.example.mxh.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    List<Posts> findByUser_IdOrderByCreatedAtDesc(Long userId);
}