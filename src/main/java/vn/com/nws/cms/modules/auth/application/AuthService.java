package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import vn.com.nws.cms.modules.auth.domain.model.RefreshToken;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.RefreshTokenRepository;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        // Delete existing refresh tokens for user
        refreshTokenRepository.deleteByUser(user);

        String refreshTokenStr = jwtProvider.generateRefreshToken(user.getUsername());
        
        // Save refresh token to DB
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshTokenStr)
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

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BusinessException("Refresh token is invalid or expired!"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        String username = user.getUsername();

        // Generate new Access Token
        String newAccessToken = jwtProvider.generateToken(username);
        String newRefreshTokenStr = jwtProvider.generateRefreshToken(username);

        // Rotate Refresh Token
        refreshToken.setToken(newRefreshTokenStr);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(username)
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User with email " + request.getEmail() + " not found"));

        String resetToken = UUID.randomUUID().toString();
        
        // TODO: Store reset token in DB or use a separate ResetToken entity
        // For simplicity in this demo without Redis, we are just logging it.
        // In production, create a PasswordResetToken entity similar to RefreshToken.
        
        log.info("Reset Password Token for {}: {}", user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // TODO: Validate token against DB
        throw new BusinessException("Reset password functionality requires DB implementation for token storage");
    }
}
