package vn.com.nws.cms.modules.iam.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Cấu hình đường dẫn lưu file avatar
    private static final String UPLOAD_DIR = "uploads/avatars/";

    @Override
    public PageResponse<UserResponse> getUsers(UserFilterRequest request) {
        List<User> users = userRepository.search(request.getKeyword(), request.getRole(), request.getPage(), request.getSize());
        long totalElements = userRepository.count(request.getKeyword(), request.getRole());
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<UserResponse> userResponses = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(userResponses)
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        Set<RoleType> roles = new HashSet<>();
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                roles.add(RoleType.valueOf(request.getRole().replace("ROLE_", "")));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid role: " + request.getRole());
            }
        } else {
            roles.add(RoleType.STUDENT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                Set<RoleType> roles = new HashSet<>();
                roles.add(RoleType.valueOf(request.getRole().replace("ROLE_", "")));
                user.setRoles(roles);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid role: " + request.getRole());
            }
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("File must be an image");
        }

        // Validate file size (ví dụ: max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("File size must not exceed 5MB");
        }

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Xóa avatar cũ nếu có
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get(user.getAvatar());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    // Log error nhưng không throw exception
                    System.err.println("Failed to delete old avatar: " + e.getMessage());
                }
            }

            // Cập nhật đường dẫn avatar
            user.setAvatar(UPLOAD_DIR + newFilename);
            user = userRepository.save(user);

            return toUserResponse(user);

        } catch (IOException e) {
            throw new BusinessException("Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new BusinessException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
