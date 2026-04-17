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
public class WeatherTool {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    public record WeatherResult(
            String city,
            double temp,
            double feelsLike,
            String description,
            int humidity,
            double windSpeed
    ) {}

    public record ForecastResult(
            String dateTime,
            double temp,
            String description
    ) {}

    public WeatherResult getWeather(String city) {
        long start = System.currentTimeMillis();
        try {
            String url = apiUrl + "/weather?q=" + java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8)
                    + "&appid=" + apiKey + "&units=metric&lang=fr";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            WeatherResult result = new WeatherResult(
                    root.path("name").asText(city),
                    root.path("main").path("temp").asDouble(),
                    root.path("main").path("feels_like").asDouble(),
                    root.path("weather").get(0).path("description").asText(""),
                    root.path("main").path("humidity").asInt(),
                    root.path("wind").path("speed").asDouble()
            );

            log.info("WeatherTool: city='{}' temp={}°C duration={}ms",
                    city, result.temp(), System.currentTimeMillis() - start);
            return result;

        } catch (Exception e) {
            log.error("WeatherTool error for {}: {}", city, e.getMessage());
            throw new RuntimeException("Weather fetch failed: " + e.getMessage(), e);
        }
    }

    public List<ForecastResult> getForecast(String city) {
        long start = System.currentTimeMillis();
        try {
            String url = apiUrl + "/forecast?q=" + java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8)
                    + "&appid=" + apiKey + "&units=metric&lang=fr&cnt=8";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            List<ForecastResult> forecasts = new ArrayList<>();
            for (JsonNode item : root.path("list")) {
                forecasts.add(new ForecastResult(
                        item.path("dt_txt").asText(),
                        item.path("main").path("temp").asDouble(),
                        item.path("weather").get(0).path("description").asText("")
                ));
            }

            log.info("WeatherTool forecast: city='{}' count={} duration={}ms",
                    city, forecasts.size(), System.currentTimeMillis() - start);
            return forecasts;

        } catch (Exception e) {
            log.error("WeatherTool forecast error for {}: {}", city, e.getMessage());
            throw new RuntimeException("Forecast fetch failed: " + e.getMessage(), e);
        }
    }
}
