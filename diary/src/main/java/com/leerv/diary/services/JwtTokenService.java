package com.leerv.diary.services;

import com.leerv.diary.entities.RefreshToken;
import com.leerv.diary.entities.User;
import com.leerv.diary.exception.AuthenticationException;
import com.leerv.diary.repositories.RefreshTokenRepository;
import com.leerv.diary.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    @Value("${application.security.jwt.access-expiration}")
    private long jwtAccessExpiration;
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtAccessExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String sessionId = UUID.randomUUID().toString();
        Map<String, Object> claims = Map.of("sessionId", sessionId);
        String newToken = buildToken(claims, userDetails, jwtRefreshExpiration);
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for given refresh token"));
        RefreshToken refreshToken = RefreshToken.builder()
                .token(newToken)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .sessionId(sessionId)
                .user(user)
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return newToken;
    }

    public boolean refreshTokenMatches(String username, String extractedToken) {
        String sessionId = extractSessionId(extractedToken);
        RefreshToken rsToken = refreshTokenRepository.findByEmailAndSessionId(username, sessionId).orElseThrow(() -> new AuthenticationException("Refresh token not found"));
        return extractedToken.equals(rsToken.getToken()) && !rsToken.isRevoked();
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long jwtExpiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !extractClaim(token, Claims::getExpiration).before(new Date(System.currentTimeMillis()));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String extractSessionId(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("sessionId", String.class);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
