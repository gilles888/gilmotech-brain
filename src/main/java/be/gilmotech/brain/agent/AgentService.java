package be.gilmotech.brain.agent;

import be.gilmotech.brain.groq.GroqClient;
import be.gilmotech.brain.groq.GroqMessage;
import be.gilmotech.brain.groq.GroqResponse;
import be.gilmotech.brain.groq.ToolCall;
import be.gilmotech.brain.memory.MemoryService;
import be.gilmotech.brain.tools.ToolsRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final GroqClient groqClient;
    private final ToolsRegistry toolsRegistry;
    private final MemoryService memoryService;

    private static final String SYSTEM_CONVERSATION =
            "Tu es GilmoBrain, assistant personnel bienveillant. Tu réponds en français, de façon claire " +
            "et chaleureuse. Tu utilises tes tools quand nécessaire pour donner des réponses précises et à jour.";

    private static final String SYSTEM_CODE =
            "Tu es GilmoBrain, assistant développeur expert en Angular, Spring Boot, Java, TypeScript, NestJS. " +
            "Tu réponds en français avec des blocs de code clairs. " +
            "Tu utilises bash et file tools pour tester et créer du code réel.";

    public AgentResult run(String task, String sessionId, String mode) {
        long start = System.currentTimeMillis();
        String systemPrompt = "code".equals(mode) ? SYSTEM_CODE : SYSTEM_CONVERSATION;

        List<GroqMessage> history = memoryService.getHistory(sessionId);
        List<GroqMessage> messages = new ArrayList<>(history);
        messages.add(new GroqMessage("user", task));

        List<String> toolsUsed = new ArrayList<>();
        int iterations = 0;

        for (int i = 0; i < 10; i++) {
            iterations++;
            GroqResponse response = groqClient.chatWithTools(systemPrompt, messages, toolsRegistry.getGroqTools());

            // Build the assistant message including tool_calls if present
            GroqMessage assistantMsg = new GroqMessage(
                    "assistant",
                    response.content(),
                    null,
                    response.toolCalls(),
                    null
            );
            messages.add(assistantMsg);

            if ("stop".equals(response.finishReason()) || response.toolCalls() == null || response.toolCalls().isEmpty()) {
                break;
            }

            for (ToolCall call : response.toolCalls()) {
                String toolName = call.name();
                String result = toolsRegistry.executeTool(toolName, call.argumentsRaw());
                toolsUsed.add(toolName);
                messages.add(new GroqMessage("tool", result, call.id()));
            }
        }

        memoryService.saveHistory(sessionId, messages);

        String finalResponse = messages.stream()
                .filter(m -> "assistant".equals(m.role()))
                .reduce((a, b) -> b)
                .map(GroqMessage::content)
                .orElse("Pas de réponse");

        long duration = System.currentTimeMillis() - start;
        log.info("AgentService: session={} mode={} iterations={} tools={} duration={}ms",
                sessionId, mode, iterations, toolsUsed, duration);

        return new AgentResult(finalResponse, sessionId, toolsUsed, duration);
    }

    public record AgentResult(String response, String sessionId, List<String> toolsUsed, long durationMs) {}
}
