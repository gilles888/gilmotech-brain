package be.gilmotech.brain.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryTool {

    private static final String PREFIX = "brain:memory:";
    private final StringRedisTemplate redisTemplate;

    public void remember(String key, String value, Long ttlSeconds) {
        String fullKey = PREFIX + key;
        if (ttlSeconds != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(fullKey, value, Duration.ofSeconds(ttlSeconds));
        } else {
            redisTemplate.opsForValue().set(fullKey, value);
        }
        log.info("MemoryTool: remember key='{}' ttl={}s", key, ttlSeconds);
    }

    public Optional<String> recall(String key) {
        String value = redisTemplate.opsForValue().get(PREFIX + key);
        log.info("MemoryTool: recall key='{}' found={}", key, value != null);
        return Optional.ofNullable(value);
    }

    public void forget(String key) {
        redisTemplate.delete(PREFIX + key);
        log.info("MemoryTool: forget key='{}'", key);
    }

    public Map<String, String> listMemories() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        Map<String, String> result = new HashMap<>();
        if (keys != null) {
            for (String fullKey : keys) {
                String shortKey = fullKey.substring(PREFIX.length());
                String value = redisTemplate.opsForValue().get(fullKey);
                if (value != null) result.put(shortKey, value);
            }
        }
        log.info("MemoryTool: listMemories count={}", result.size());
        return result;
    }
}
