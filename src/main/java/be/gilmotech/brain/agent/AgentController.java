package be.gilmotech.brain.agent;

import be.gilmotech.brain.groq.GroqMessage;
import be.gilmotech.brain.memory.MemoryService;
import be.gilmotech.brain.tools.ToolsRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final MemoryService memoryService;
    private final ToolsRegistry toolsRegistry;

    @Value("${server.port:4000}")
    private String serverPort;

    @Value("${groq.model}")
    private String groqModel;

    @PostMapping("/agent/run")
    public ResponseEntity<Map<String, Object>> run(@Valid @RequestBody AgentRequest request) {
        log.info("AgentController: POST /agent/run session={} mode={}", request.sessionId(), request.resolvedMode());
        AgentService.AgentResult result = agentService.run(request.task(), request.sessionId(), request.resolvedMode());
        return ResponseEntity.ok(Map.of(
                "response", result.response() != null ? result.response() : "",
                "sessionId", result.sessionId(),
                "toolsUsed", result.toolsUsed(),
                "durationMs", result.durationMs()
        ));
    }

    @GetMapping("/agent/memory/{sessionId}")
    public ResponseEntity<Map<String, Object>> getMemory(@PathVariable String sessionId) {
        List<GroqMessage> messages = memoryService.getRawHistory(sessionId);
        return ResponseEntity.ok(Map.of("messages", messages));
    }

    @DeleteMapping("/agent/memory/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteMemory(@PathVariable String sessionId) {
        memoryService.deleteHistory(sessionId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        List<String> toolNames = toolsRegistry.getGroqTools().stream()
                .map(t -> t.function().name())
                .toList();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "model", groqModel,
                "port", serverPort,
                "toolsAvailable", toolNames
        ));
    }
}
