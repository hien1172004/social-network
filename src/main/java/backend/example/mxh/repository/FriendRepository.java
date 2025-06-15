package backend.example.mxh.repository;

import backend.example.mxh.entity.Friend;
import backend.example.mxh.until.FriendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query(value = """
            select case when count(*) > 0 then true else false end
            from friends where (sender_id = :id1 and receiver_id = :id2)
            or (sender_id = :id2 and receiver_id = :id1)
            """, nativeQuery = true)
    boolean existsFriendship(@Param("id1") Long id1, @Param("id2") Long id2);


    Page<Friend> findByReceiver_IdAndStatus(Long receiverId, FriendStatus status, Pageable pageable);


    Page<Friend> findBySender_IdAndStatus(Long senderId, FriendStatus status, Pageable pageable);

    @Query("""
     SELECT f FROM Friend f
            WHERE (f.sender.id = :userId OR f.receiver.id = :userId)
            AND f.status = :status
""")
    Page<Friend> findAcceptedFriends(@Param("id") long userId, @Param("status") FriendStatus status, Pageable pageable);

    @Modifying
    @Query("""
    DELETE FROM Friend f
    WHERE ((f.sender.id = :userId1 AND f.receiver.id = :userId2)
       OR  (f.sender.id = :userId2 AND f.receiver.id = :userId1))
      AND f.status = :status
""")
    void unfriend(Long userId1, Long userId2, FriendStatus status);

    @Query("""
    SELECT CASE
             WHEN f.sender.id = :userId THEN f.receiver.id
             ELSE f.sender.id
           END
    FROM Friend f
    WHERE (f.sender.id = :userId OR f.receiver.id = :userId)
      AND f.status = :status
""")
    List<Long> findFriendIds(@Param("userId") Long userId, @Param("status") FriendStatus status);
}