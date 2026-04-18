package be.gilmotech.brain.users;

import be.gilmotech.brain.users.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${brain.avatars.path}")
    private String avatarsPath;

    @Value("${brain.avatars.url}")
    private String avatarsUrl;

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    public UserProfileDto getUser(String userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    @Transactional
    public UserProfileDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.username())
                .displayName(request.displayName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .avatarColor(request.avatarColor() != null ? request.avatarColor() : "#7C3AED")
                .preferredMode(request.preferredMode() != null ? request.preferredMode() : "conversation")
                .build();
        userRepository.save(user);
        log.info("UserService: user created id={} username={}", user.getId(), user.getUsername());
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.avatarColor() != null) user.setAvatarColor(request.avatarColor());
        if (request.preferredMode() != null) user.setPreferredMode(request.preferredMode());
        user.setUpdatedAt(LocalDateTime.now());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(String userId, MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.contains("jpeg") && !contentType.contains("jpg")
                && !contentType.contains("png"))) {
            throw new IllegalArgumentException("Format non supporté (JPG ou PNG uniquement)");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 2 Mo)");
        }

        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) throw new IllegalArgumentException("Image illisible");

        BufferedImage resized = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, 200, 200, null);
        g.dispose();

        new File(avatarsPath).mkdirs();
        File output = new File(avatarsPath + "/" + userId + ".jpg");
        ImageIO.write(resized, "jpg", output);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        String url = avatarsUrl + "/" + userId + ".jpg";
        user.setAvatarUrl(url);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("UserService: avatar uploaded userId={}", userId);
        return url;
    }

    @Transactional
    public void deactivateUser(String userId) {
        if ("user1".equals(userId)) throw new IllegalArgumentException("Impossible de désactiver Gil");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("UserService: user deactivated id={}", userId);
    }

    private UserProfileDto toDto(User u) {
        return new UserProfileDto(u.getId(), u.getUsername(), u.getDisplayName(),
                u.getAvatarUrl(), u.getAvatarColor(), u.getRole(), u.getPreferredMode(), u.getCreatedAt());
    }
}
