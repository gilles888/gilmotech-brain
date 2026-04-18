package be.gilmotech.brain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findByToken(String token);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);

    @Transactional
    void deleteByUserId(String userId);
}
