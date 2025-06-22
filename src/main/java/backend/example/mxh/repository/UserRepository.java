package backend.example.mxh.repository;

import backend.example.mxh.entity.User;
import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
    SELECT distinct u from User u where LOWER(u.fullName) like lower(CONCAT('%', :key, '%')) 
    OR lower(u.username) like lower(CONCAT('%', :key, '%')) 
    AND u.accountStatus = :accountStatus
    """)
    Page<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(Pageable pageable ,@Param("key") String key, @Param("accountStatus") AccountStatus accountStatus);

    Page<User> findByIdIn(Collection<Long> ids, Pageable pageable);

    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByStatus(UserStatus status);

    List<User> findByStatusAndLastActiveBefore(UserStatus status, LocalDateTime lastActiveBefore);
}