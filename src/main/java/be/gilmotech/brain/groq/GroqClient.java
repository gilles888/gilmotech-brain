package be.gilmotech.brain.groq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    public String chat(String systemPrompt, List<GroqMessage> messages) {
        GroqResponse response = chatWithTools(systemPrompt, messages, null, 0.3, 2048);
        return response.content();
    }

    public GroqResponse chatWithTools(String systemPrompt, List<GroqMessage> messages, List<GroqTool> tools) {
        return chatWithTools(systemPrompt, messages, tools, 0.3, 2048);
    }

    public GroqResponse chatWithTools(String systemPrompt, List<GroqMessage> messages, List<GroqTool> tools, double temperature, int maxTokens) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);

            ArrayNode msgs = objectMapper.createArrayNode();

            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            msgs.add(systemMsg);

            for (GroqMessage m : messages) {
                msgs.add(objectMapper.valueToTree(m));
            }
            body.set("messages", msgs);

            if (tools != null && !tools.isEmpty()) {
                body.set("tools", objectMapper.valueToTree(tools));
                body.put("tool_choice", "auto");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choice = root.path("choices").get(0);
            JsonNode message = choice.path("message");
            String finishReason = choice.path("finish_reason").asText("stop");

            String content = message.path("content").isNull() ? null : message.path("content").asText(null);

            List<ToolCall> toolCalls = null;
            if (message.has("tool_calls") && !message.path("tool_calls").isNull()) {
                toolCalls = new ArrayList<>();
                for (JsonNode tc : message.path("tool_calls")) {
                    String id = tc.path("id").asText();
                    String name = tc.path("function").path("name").asText();
                    String arguments = tc.path("function").path("arguments").asText();
                    toolCalls.add(new ToolCall(id, "function", new ToolCall.Function(name, arguments)));
                }
            }

            log.debug("Groq response — finish_reason={}, toolCalls={}", finishReason, toolCalls != null ? toolCalls.size() : 0);
            return new GroqResponse(content, toolCalls, finishReason);

        } catch (Exception e) {
            log.error("Groq API error: {}", e.getMessage());
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }
}
