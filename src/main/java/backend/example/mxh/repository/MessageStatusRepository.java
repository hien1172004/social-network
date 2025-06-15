package backend.example.mxh.repository;

import backend.example.mxh.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {
}