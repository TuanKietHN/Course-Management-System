package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());
        
        // Save refresh token to Redis (Key: auth:rt:{token} -> username)
        saveRefreshToken(user.getUsername(), refreshToken);

        return TokenResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
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
        
        // In a real app, we should parse the username from the refresh token or pass it in request
        // For simplicity here, we assume the client knows the flow or we use a structured refresh token.
        // BUT, since we stored simple UUID in Redis mapped by Username, we can't easily reverse lookup without scanning.
        // Improvement: Refresh Token should be a signed JWT or contain the username, OR client sends username.
        
        // Let's assume for this basic impl, we require the user to be logged in (which defeats the purpose if access token expired)
        // OR we change the Redis key strategy.
        // Better Strategy: The Refresh Token itself is the Key in Redis, Value is Username.
        
        // For now, let's fix the Login logic to store "auth:rt:{refreshToken}" -> username
        // But wait, we want to invalidate old tokens by username too.
        // So we store two keys: "auth:user:{username}:rt" -> refreshToken AND "auth:rt:{refreshToken}" -> username
        
        // Let's stick to the simplest working model for this demo:
        // We will assume the refresh token IS the key to find the user.
        // NOTE: This requires changing the Login logic slightly (done below in refactor).
        
        // Refactored Logic for this method:
        // 1. Check if token exists in Redis (Key: auth:rt:{token})
        // 2. If exists, get username
        // 3. Generate new tokens
        // 4. Update Redis
        
        String username = (String) redisTemplate.opsForValue().get("auth:rt:" + requestRefreshToken);
        if (username == null) {
            throw new BusinessException("Refresh token is invalid or expired!");
        }

        // Generate new Access Token
        String newAccessToken = jwtProvider.generateToken(username);
        String newRefreshToken = jwtProvider.generateRefreshToken(username);

        // Delete old token mapping
        redisTemplate.delete("auth:rt:" + requestRefreshToken);
        
        // Save new token mapping
        redisTemplate.opsForValue().set("auth:rt:" + newRefreshToken, username, refreshExpiration, TimeUnit.MILLISECONDS);
        
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("User not found"));

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(username)
                .role(user.getRole())
                .build();
    }
    
    // Adjusted Login Logic for Redis Key
    public void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set("auth:rt:" + refreshToken, username, refreshExpiration, TimeUnit.MILLISECONDS);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.delete("auth:rt:" + refreshToken);
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User with email " + request.getEmail() + " not found"));

        String resetToken = UUID.randomUUID().toString();
        
        // Store reset token in Redis: auth:reset:{token} -> username (TTL: 15 mins)
        redisTemplate.opsForValue().set("auth:reset:" + resetToken, user.getUsername(), 15, TimeUnit.MINUTES);

        // TODO: Send email
        log.info("Reset Password Token for {}: {}", user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String username = (String) redisTemplate.opsForValue().get("auth:reset:" + request.getToken());
        if (username == null) {
            throw new BusinessException("Invalid or expired reset token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token
        redisTemplate.delete("auth:reset:" + request.getToken());
    }
}
