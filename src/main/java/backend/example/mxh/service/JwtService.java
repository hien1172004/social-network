package backend.example.mxh.service;

import backend.example.mxh.until.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserDetails user);

    String extractUsername(String token, TokenType type);

    boolean isValid(String token, TokenType type, UserDetails userDetails);

    String generateRefreshToken(UserDetails user);

    String generateResetToken(UserDetails userDetails);

    String generateVerificationToken(UserDetails userDetails);

}
