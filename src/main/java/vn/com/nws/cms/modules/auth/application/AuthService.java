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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.common.security.JwtProvider;
import vn.com.nws.cms.modules.auth.api.dto.*;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String REDIS_RT_KEY_PREFIX = "auth:rt:"; // Key: token -> Value: username
    private static final String REDIS_USER_RT_KEY_PREFIX = "auth:u:rt:"; // Key: username -> Value: token (Single Session enforcement)
    private static final String REDIS_RESET_TOKEN_PREFIX = "auth:reset:";

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

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("Email is already in use!");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole() != null ? "ROLE_" + registerRequest.getRole() : "ROLE_STUDENT")
                .build();

        userRepository.save(user);
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
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
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

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User with email " + request.getEmail() + " not found"));

        String resetToken = UUID.randomUUID().toString();
        String key = REDIS_RESET_TOKEN_PREFIX + resetToken;
        
        // Store in Redis: key=token, value=email, ttl=15 min
        redisTemplate.opsForValue().set(key, user.getEmail(), Duration.ofMinutes(15));
        
        log.info("Reset Password Token for {}: {}", user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String key = REDIS_RESET_TOKEN_PREFIX + request.getToken();
        String email = (String) redisTemplate.opsForValue().get(key);
        
        if (email == null) {
            throw new BusinessException("Invalid or expired reset token");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
                
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Invalidate token
        redisTemplate.delete(key);
        
        // Optional: Invalidate all sessions (force login)
        redisTemplate.delete(REDIS_USER_RT_KEY_PREFIX + user.getUsername());
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
                .role(user.getRole())
                .build();
    }
}
