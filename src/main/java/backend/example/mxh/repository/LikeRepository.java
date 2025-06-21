package backend.example.mxh.repository;

import backend.example.mxh.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUser_IdAndPosts_Id(Long userId, Long postsId);

    long countByPosts_Id(Long postsId);

    Page<Like> findByPosts_Id(Long postsId, Pageable pageable);
}