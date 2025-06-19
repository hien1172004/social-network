package backend.example.mxh.repository;

import backend.example.mxh.entity.User;
import backend.example.mxh.until.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
    SELECT distinct u from User u where LOWER(u.fullName) like lower(CONCAT('%', :key, '%')) 
    OR lower(u.fullName) like lower(CONCAT('%', :key, '%')) 
    """)
    Page<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(Pageable pageable ,@Param("key") String key);

    Page<User> findByIdIn(Collection<Long> ids, Pageable pageable);

    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByStatus(UserStatus status);

    List<User> findByStatusAndLastActiveBefore(UserStatus status, LocalDateTime lastActiveBefore);
}