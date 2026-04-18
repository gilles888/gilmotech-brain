package be.gilmotech.brain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameAndActiveTrue(String username);
    List<User> findByActiveTrue();
    boolean existsByUsername(String username);
}
