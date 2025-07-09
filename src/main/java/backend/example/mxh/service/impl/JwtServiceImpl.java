package backend.example.mxh.service.impl;

import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.service.JwtService;
import backend.example.mxh.until.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


import static backend.example.mxh.until.TokenType.VERIFICATION_TOKEN;


@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    @Value(("${jwt.expiryHour}"))
    private long expiryHour;

    @Value(("${jwt.expiryDay}"))
    private long expiryDay;

    @Value(("${jwt.secretKey}"))
    private String secretKey;

    @Value(("${jwt.refreshKey}"))
    private String refreshKey;

    @Value(("${jwt.resetKey}"))
    private String resetKey;

    @Value(("${jwt.verifyKey}"))
    private String verifyKey;

    private static final String ISSUER = "mxh-backend";
    private static final String AUDIENCE = "mxh-frontend";

    @Override
    public String generateToken(UserDetails user) {
        return generateToken(new HashMap<>(), user);
    }

    @Override
    public String extractUsername(String token, TokenType type) {
        return extractClaim(token, type, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, TokenType type, UserDetails userDetails) {
        log.info("---------- isValid ----------");
        final String email = extractUsername(token, type);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token, type));
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    @Override
    public String generateResetToken(UserDetails user) {
        return generateResetToken(new HashMap<>(), user);
    }

    @Override
    public String generateVerificationToken(UserDetails userDetails) {
        return generateVerificationToken(new HashMap<>(), userDetails);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        claims.put("role", ((User) userDetails).getRole().name());
        claims.put("tokenType", TokenType.ACCESS_TOKEN.toString());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date((System.currentTimeMillis()) + 1000 * 60 * 60  * expiryHour))
                .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date((System.currentTimeMillis()) + 1000 * 60 * 60 * 24 * expiryDay))
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateResetToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date((System.currentTimeMillis()) + 1000 * 60 * 60))
                .signWith(getKey(TokenType.RESET_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateVerificationToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getKey(VERIFICATION_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType type) {
        switch (type) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            case RESET_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(resetKey));
            }
            case VERIFICATION_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(verifyKey));
            }
            default -> throw new InvalidDataException("token type invalid");
        }
    }

    private <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimsResolver) {
        final Claims claims = extraAllClaim(token, type);
        if (!ISSUER.equals(claims.getIssuer())) {
            throw new InvalidDataException("Invalid issuer");
        }
        if (!AUDIENCE.equals(claims.getAudience())) {
            throw new InvalidDataException("Invalid audience");
        }
        return claimsResolver.apply(claims);
    }

    private Claims extraAllClaim(String token, TokenType type) {
        log.info("Extract all claims for token {}...", token.substring(0, 15));
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey(type))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new InvalidDataException("Invalid or expired JWT token");
        }
    }

    public String extractRole(String token) {
        Claims claims = extraAllClaim(token, TokenType.ACCESS_TOKEN);
        return claims.get("role", String.class); // ROLE_USER
    }
    private boolean isTokenExpired(String token, TokenType type) {
        Date expiration = extractExpiration(token, type);
        return expiration != null && expiration.before(new Date());
    }

    private Date extractExpiration(String token, TokenType type) {
        return extractClaim(token, type, Claims::getExpiration);
    }
}
