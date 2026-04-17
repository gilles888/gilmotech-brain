package be.gilmotech.brain.memory;

import be.gilmotech.brain.groq.GroqMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private static final String PREFIX = "brain:session:";
    private static final int MAX_MESSAGES = 20;
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<GroqMessage> getHistory(String sessionId) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + sessionId);
            if (json == null) return new ArrayList<>();
            List<GroqMessage> history = objectMapper.readValue(json, new TypeReference<>() {});
            if (history.size() > MAX_MESSAGES) {
                history = history.subList(history.size() - MAX_MESSAGES, history.size());
            }
            log.debug("MemoryService: loaded {} messages for session {}", history.size(), sessionId);
            return new ArrayList<>(history);
        } catch (Exception e) {
            log.error("MemoryService: failed to load history for {}: {}", sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveHistory(String sessionId, List<GroqMessage> messages) {
        try {
            List<GroqMessage> toSave = messages;
            if (messages.size() > MAX_MESSAGES) {
                toSave = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
            }
            String json = objectMapper.writeValueAsString(toSave);
            redisTemplate.opsForValue().set(PREFIX + sessionId, json, SESSION_TTL);
            log.debug("MemoryService: saved {} messages for session {}", toSave.size(), sessionId);
        } catch (Exception e) {
            log.error("MemoryService: failed to save history for {}: {}", sessionId, e.getMessage());
        }
    }

    public void deleteHistory(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId);
        log.info("MemoryService: deleted session {}", sessionId);
    }

    public List<GroqMessage> getRawHistory(String sessionId) {
        return getHistory(sessionId);
    }
}
