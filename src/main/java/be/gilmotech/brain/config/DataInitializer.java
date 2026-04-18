package be.gilmotech.brain.config;

import be.gilmotech.brain.users.User;
import be.gilmotech.brain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent("user1", "gil", "Gil", "GilAdmin2025x", "#7C3AED", "admin", "code");
        createIfAbsent("user2", "riri", "Riri", "Riri2025x", "#F59E0B", "member", "conversation");
    }

    private void createIfAbsent(String id, String username, String displayName,
                                 String password, String color, String role, String mode) {
        if (!userRepository.existsById(id)) {
            User user = User.builder()
                    .id(id)
                    .username(username)
                    .displayName(displayName)
                    .passwordHash(passwordEncoder.encode(password))
                    .avatarColor(color)
                    .role(role)
                    .preferredMode(mode)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            log.info("DataInitializer: created user {} ({})", username, id);
        }
    }
}
