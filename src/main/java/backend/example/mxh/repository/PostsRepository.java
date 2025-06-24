package backend.example.mxh.repository;

import backend.example.mxh.entity.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {

    @EntityGraph(attributePaths = {
            "user",              // người đăng bài
            "postImage",         // ảnh đính kèm
            "likes",             // danh sách like
            "likes.user",        // người đã like
            "comments",          // bình luận
            "comments.user"      // người bình luận
    })
    Optional<Posts> findWithDetailsById(Long id);



    Page<Posts> findAll(@NonNull Pageable pageable);


    Page<Posts> findByUser_IdOrderByCreatedAtDesc(long userId, Pageable pageable);

    @Query("""
select p from Posts p
where p.user.id = ?1
and p.createdAt >= ?2 and p.createdAt <= ?3
""")
    Page<Posts> getPostsOfUserInTime(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Posts> findByUser_Id(Long userId, Pageable pageable);
}