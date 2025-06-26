package backend.example.mxh.service;

import backend.example.mxh.entity.RedisToken;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisTokenService {
    private final RedisTokenRepository redisTokenRepository;

    public String save(RedisToken redisToken) {
        RedisToken result =  redisTokenRepository.save(redisToken);
        return result.getId();
    }

    public void delete(String id) {
        redisTokenRepository.deleteById(id);
    }

    public RedisToken getById(String email) {
        return  redisTokenRepository.findById(email).orElseThrow(()-> new ResourceNotFoundException("Email not found"));
    }
}
