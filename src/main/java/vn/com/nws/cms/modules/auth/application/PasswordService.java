package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.auth.api.dto.ForgotPasswordRequest;
import vn.com.nws.cms.modules.auth.api.dto.ResetPasswordRequest;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final vn.com.nws.cms.infrastructure.messaging.EmailProducer emailProducer;

    private static final String REDIS_RESET_TOKEN_PREFIX = "auth:reset:";
    private static final String REDIS_USER_RT_KEY_PREFIX = "auth:u:rt:"; // Needed to invalidate sessions

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User with email " + request.getEmail() + " not found"));

        String resetToken = UUID.randomUUID().toString();
        String key = REDIS_RESET_TOKEN_PREFIX + resetToken;
        
        // Store in Redis: key=token, value=email, ttl=15 min
        redisTemplate.opsForValue().set(key, user.getEmail(), Duration.ofMinutes(15));
        
        log.info("Reset Password Token for {}: {}", user.getEmail(), resetToken);
        
        // Send Reset Password Email asynchronously
        emailProducer.sendEmail(vn.com.nws.cms.common.dto.EmailMessage.builder()
                .to(user.getEmail())
                .subject("Reset Password Request")
                .body("Your reset token is: " + resetToken)
                .type("FORGOT_PASSWORD")
                .build());
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
}
