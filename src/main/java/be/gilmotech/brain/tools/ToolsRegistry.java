package be.gilmotech.brain.tools;

import be.gilmotech.brain.groq.GroqTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolsRegistry {

    private final WebSearchTool webSearchTool;
    private final BashTool bashTool;
    private final FileTool fileTool;
    private final WeatherTool weatherTool;
    private final MemoryTool memoryTool;
    private final ObjectMapper objectMapper;

    public List<GroqTool> getGroqTools() {
        return List.of(
                new GroqTool("web_search", "Recherche sur le web via Brave Search",
                        Map.of("type", "object",
                                "properties", Map.of("query", Map.of("type", "string", "description", "Requête de recherche")),
                                "required", List.of("query"))),

                new GroqTool("bash_execute", "Exécute une commande bash dans le sandbox /opt/brain-sandbox",
                        Map.of("type", "object",
                                "properties", Map.of("command", Map.of("type", "string", "description", "Commande bash à exécuter")),
                                "required", List.of("command"))),

                new GroqTool("file_read", "Lit un fichier dans le sandbox",
                        Map.of("type", "object",
                                "properties", Map.of("path", Map.of("type", "string", "description", "Chemin relatif au sandbox")),
                                "required", List.of("path"))),

                new GroqTool("file_write", "Écrit dans un fichier du sandbox",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "path", Map.of("type", "string", "description", "Chemin relatif au sandbox"),
                                        "content", Map.of("type", "string", "description", "Contenu à écrire")),
                                "required", List.of("path", "content"))),

                new GroqTool("file_list", "Liste les fichiers d'un répertoire dans le sandbox",
                        Map.of("type", "object",
                                "properties", Map.of("path", Map.of("type", "string", "description", "Répertoire relatif au sandbox (. pour la racine)")),
                                "required", List.of("path"))),

                new GroqTool("weather_current", "Météo actuelle d'une ville",
                        Map.of("type", "object",
                                "properties", Map.of("city", Map.of("type", "string", "description", "Nom de la ville")),
                                "required", List.of("city"))),

                new GroqTool("weather_forecast", "Prévisions météo pour une ville",
                        Map.of("type", "object",
                                "properties", Map.of("city", Map.of("type", "string", "description", "Nom de la ville")),
                                "required", List.of("city"))),

                new GroqTool("memory_remember", "Mémorise une information clé-valeur",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "key", Map.of("type", "string"),
                                        "value", Map.of("type", "string"),
                                        "ttl_seconds", Map.of("type", "integer", "description", "Durée de vie en secondes (optionnel)")),
                                "required", List.of("key", "value"))),

                new GroqTool("memory_recall", "Rappelle une information mémorisée",
                        Map.of("type", "object",
                                "properties", Map.of("key", Map.of("type", "string")),
                                "required", List.of("key"))),

                new GroqTool("memory_forget", "Supprime une information mémorisée",
                        Map.of("type", "object",
                                "properties", Map.of("key", Map.of("type", "string")),
                                "required", List.of("key"))),

                new GroqTool("memory_list", "Liste toutes les informations mémorisées",
                        Map.of("type", "object", "properties", Map.of()))
        );
    }

    @SuppressWarnings("unchecked")
    public String executeTool(String name, String argumentsJson) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> args = argumentsJson != null
                    ? objectMapper.readValue(argumentsJson, Map.class)
                    : Map.of();

            String result = switch (name) {
                case "web_search" -> {
                    var results = webSearchTool.search((String) args.get("query"));
                    yield results.stream()
                            .map(r -> "**" + r.title() + "**\n" + r.url() + "\n" + r.description())
                            .collect(Collectors.joining("\n\n"));
                }
                case "bash_execute" -> {
                    var r = bashTool.execute((String) args.get("command"));
                    yield (r.timedOut() ? "[TIMEOUT] " : "") +
                            (r.stdout().isBlank() ? "" : r.stdout()) +
                            (r.stderr().isBlank() ? "" : "\nSTDERR: " + r.stderr()) +
                            "\n[exit: " + r.exitCode() + "]";
                }
                case "file_read" -> fileTool.readFile((String) args.get("path"));
                case "file_write" -> {
                    fileTool.writeFile((String) args.get("path"), (String) args.get("content"));
                    yield "Fichier écrit avec succès : " + args.get("path");
                }
                case "file_list" -> String.join("\n", fileTool.listFiles((String) args.get("path")));
                case "weather_current" -> {
                    var w = weatherTool.getWeather((String) args.get("city"));
                    yield String.format("Météo à %s : %.1f°C (ressenti %.1f°C), %s, humidité %d%%, vent %.1f m/s",
                            w.city(), w.temp(), w.feelsLike(), w.description(), w.humidity(), w.windSpeed());
                }
                case "weather_forecast" -> {
                    var forecasts = weatherTool.getForecast((String) args.get("city"));
                    yield forecasts.stream()
                            .map(f -> f.dateTime() + " : " + String.format("%.1f°C", f.temp()) + " — " + f.description())
                            .collect(Collectors.joining("\n"));
                }
                case "memory_remember" -> {
                    Long ttl = args.get("ttl_seconds") != null
                            ? ((Number) args.get("ttl_seconds")).longValue() : null;
                    memoryTool.remember((String) args.get("key"), (String) args.get("value"), ttl);
                    yield "Mémorisé : " + args.get("key");
                }
                case "memory_recall" -> memoryTool.recall((String) args.get("key"))
                        .orElse("Aucune valeur trouvée pour : " + args.get("key"));
                case "memory_forget" -> {
                    memoryTool.forget((String) args.get("key"));
                    yield "Oublié : " + args.get("key");
                }
                case "memory_list" -> {
                    var memories = memoryTool.listMemories();
                    yield memories.isEmpty() ? "Aucune mémoire enregistrée." :
                            memories.entrySet().stream()
                                    .map(e -> e.getKey() + " = " + e.getValue())
                                    .collect(Collectors.joining("\n"));
                }
                default -> "Tool inconnu : " + name;
            };

            log.info("ToolsRegistry: tool='{}' duration={}ms result={}",
                    name, System.currentTimeMillis() - start,
                    result.length() > 100 ? result.substring(0, 100) + "..." : result);
            return result;

        } catch (Exception e) {
            log.error("ToolsRegistry: tool='{}' error={}", name, e.getMessage());
            return "Erreur lors de l'exécution du tool '" + name + "': " + e.getMessage();
        }
    }
}
