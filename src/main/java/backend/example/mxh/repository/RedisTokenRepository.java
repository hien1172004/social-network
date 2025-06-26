package backend.example.mxh.repository;

import backend.example.mxh.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository  extends CrudRepository<RedisToken, String> {
}
