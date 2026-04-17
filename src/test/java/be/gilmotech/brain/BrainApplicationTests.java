package be.gilmotech.brain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "groq.api.key=test-key",
        "brave.api.key=test-key",
        "weather.api.key=test-key",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
class BrainApplicationTests {

    @Test
    void contextLoads() {
    }
}
