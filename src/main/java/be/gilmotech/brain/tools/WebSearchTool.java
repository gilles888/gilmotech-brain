package be.gilmotech.brain.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class WebSearchTool {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${brave.api.key}")
    private String apiKey;

    @Value("${brave.api.url}")
    private String apiUrl;

    public record SearchResult(String title, String url, String description) {}

    public List<SearchResult> search(String query) {
        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Subscription-Token", apiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            String url = apiUrl + "?q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8) + "&count=5&lang=fr";
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("web").path("results");

            List<SearchResult> out = new ArrayList<>();
            for (JsonNode r : results) {
                out.add(new SearchResult(
                        r.path("title").asText(""),
                        r.path("url").asText(""),
                        r.path("description").asText("")
                ));
            }

            log.info("WebSearchTool: query='{}' results={} duration={}ms",
                    query, out.size(), System.currentTimeMillis() - start);
            return out;

        } catch (Exception e) {
            log.error("WebSearchTool error: {}", e.getMessage());
            throw new RuntimeException("Web search failed: " + e.getMessage(), e);
        }
    }
}
