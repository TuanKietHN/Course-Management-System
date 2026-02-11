package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.common.security.JwtProvider;
import vn.com.nws.cms.modules.auth.api.dto.LoginRequest;
import vn.com.nws.cms.modules.auth.api.dto.RefreshTokenRequest;
import vn.com.nws.cms.modules.auth.api.dto.TokenResponse;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String REDIS_RT_KEY_PREFIX = "auth:rt:"; // Key: token -> Value: username
    private static final String REDIS_USER_RT_KEY_PREFIX = "auth:u:rt:"; // Key: username -> Value: token (Single Session enforcement)

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        // Handle Refresh Token
        String refreshToken = UUID.randomUUID().toString();
        saveRefreshTokenToRedis(user.getUsername(), refreshToken);

        return buildTokenResponse(jwt, refreshToken, user);
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        // 1. Find username by token
        String username = (String) redisTemplate.opsForValue().get(REDIS_RT_KEY_PREFIX + requestRefreshToken);
        if (username == null) {
            throw new BusinessException("Refresh token is invalid or expired!");
        }

        // 2. Validate Single Session (Token Reuse Detection)
        String currentActiveToken = (String) redisTemplate.opsForValue().get(REDIS_USER_RT_KEY_PREFIX + username);
        if (!requestRefreshToken.equals(currentActiveToken)) {
            // Token mismatch! Possible theft. Invalidate everything for this user.
            redisTemplate.delete(REDIS_USER_RT_KEY_PREFIX + username);
            redisTemplate.delete(REDIS_RT_KEY_PREFIX + requestRefreshToken);
            if (currentActiveToken != null) {
                redisTemplate.delete(REDIS_RT_KEY_PREFIX + currentActiveToken);
            }
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }

        // 3. Rotate Token
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        // Generate new JWT
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), 
                null, 
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.authority()))
                    .collect(Collectors.toList())
        );
        String newAccessToken = jwtProvider.generateToken(auth);
        
        // Generate new Refresh Token
        String newRefreshToken = UUID.randomUUID().toString();
        
        // Cleanup old token
        redisTemplate.delete(REDIS_RT_KEY_PREFIX + requestRefreshToken);
        
        // Save new token
        saveRefreshTokenToRedis(username, newRefreshToken);

        return buildTokenResponse(newAccessToken, newRefreshToken, user);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            String username = (String) redisTemplate.opsForValue().get(REDIS_RT_KEY_PREFIX + refreshToken);
            if (username != null) {
                redisTemplate.delete(REDIS_RT_KEY_PREFIX + refreshToken);
                redisTemplate.delete(REDIS_USER_RT_KEY_PREFIX + username);
            }
        }
    }

    private void saveRefreshTokenToRedis(String username, String refreshToken) {
        // Enforce Single Session: Invalidate old token if exists
        String oldToken = (String) redisTemplate.opsForValue().get(REDIS_USER_RT_KEY_PREFIX + username);
        if (oldToken != null) {
            redisTemplate.delete(REDIS_RT_KEY_PREFIX + oldToken);
        }

        // Save new token
        redisTemplate.opsForValue().set(REDIS_RT_KEY_PREFIX + refreshToken, username, Duration.ofMillis(refreshExpiration));
        redisTemplate.opsForValue().set(REDIS_USER_RT_KEY_PREFIX + username, refreshToken, Duration.ofMillis(refreshExpiration));
    }

    private TokenResponse buildTokenResponse(String accessToken, String refreshToken, User user) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .role(user.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                .build();
    }
}
