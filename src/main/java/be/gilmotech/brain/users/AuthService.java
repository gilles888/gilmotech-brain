package be.gilmotech.brain.users;

import be.gilmotech.brain.users.dto.LoginRequest;
import be.gilmotech.brain.users.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndActiveTrue(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants incorrects");
        }

        String token = generateToken();
        UserSession session = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        sessionRepository.save(session);

        log.info("AuthService: login userId={} username={}", user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getDisplayName(),
                user.getAvatarUrl(), user.getAvatarColor(), user.getRole(), user.getPreferredMode());
    }

    public User validateToken(String token) {
        UserSession session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new IllegalArgumentException("Token expiré");
        }

        return userRepository.findById(session.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    @Transactional
    public void logout(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            sessionRepository.delete(session);
            log.info("AuthService: logout userId={}", session.getUserId());
        });
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredSessions() {
        sessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("AuthService: expired sessions cleaned");
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }
}
