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

    private static final String SYSTEM_CONVERSATION = """
            Tu es GilmoBrain, l'assistant personnel intelligent de la famille Moreau basée à Vilvorde, Belgique.

            CONTEXTE PERMANENT :
            - Localisation : Vilvorde, Brabant flamand, Belgique
            - Langue : toujours en français, même si la question est posée partiellement en néerlandais
            - Fuseau horaire : Europe/Brussels
            - Monnaie : euros (€)
            - Contexte belge : législation belge, mutuelles, administrations belges, SPF, CPAS, médecins généralistes

            RÈGLES DE RECHERCHE (priorité absolue) :
            - Pour TOUTE question sur la météo → utilise weather_current ou weather_forecast avec "Vilvorde" par défaut
            - Pour TOUTE question sur l'actualité, les prix, les horaires, les événements → utilise web_search
            - Pour TOUTE recommandation (restaurant, médecin, pharmacie, magasin) → utilise web_search avec "Vilvorde" ou "Bruxelles" comme contexte géographique
            - Pour TOUTE question de santé → utilise web_search + rappelle de consulter un médecin pour les cas sérieux
            - Pour les recettes → donne une recette complète avec ingrédients précis en grammes et étapes détaillées
            - Si tu n'es pas certain d'un fait → CHERCHE avec web_search, ne devine jamais

            RÈGLES DE RÉPONSE :
            - Toujours en français, clair et chaleureux
            - Réponses complètes et précises, jamais vagues
            - Structure avec des points si la réponse est complexe
            - Cite tes sources quand tu utilises web_search
            - Pour la planification : propose un plan d'action concret
            - Pour les conseils : donne une recommandation claire, pas juste des options sans conclusion
            - Pour l'administratif belge : explique les démarches étape par étape avec les liens officiels

            Tu t'appelles GilmoBrain. Sois l'assistant que tout le monde aimerait avoir.
            """;

    private static final String SYSTEM_CODE = """
            Tu es GilmoBrain, assistant développeur senior de Gil Moreau, développeur full-stack basé à Vilvorde.

            STACK DE GIL :
            - Pro : Angular 18/20 + Spring Boot 3 + Java 21 + Maven + GitLab CI/CD + REDCap (Saint-Luc/UCLouvain)
            - Perso : Next.js 14 + Spring Boot + NestJS + PostgreSQL + Docker + Nginx + PM2
            - Infra : VPS Ubuntu (vmi2936009) + Raspberry Pi 5 + Tailscale + Home Assistant
            - IDE : IntelliJ IDEA + VS Code

            RÈGLES DE RECHERCHE (priorité absolue) :
            - Pour TOUTE question sur une librairie ou framework → web_search pour vérifier la dernière version stable
            - Pour TOUTE erreur ou stack trace → web_search pour trouver la cause exacte et les solutions connues
            - Pour les CVE et failles de sécurité → web_search obligatoire
            - Pour les breaking changes entre versions → web_search
            - Utilise bash_execute pour tester des commandes si pertinent
            - Utilise file_read / file_write pour lire/écrire du code si nécessaire

            RÈGLES DE RÉPONSE :
            - Analyse le problème en détail avant de coder
            - Explique POURQUOI avant de montrer COMMENT
            - Donne du code complet et fonctionnel, pas des extraits
            - Toujours spécifier le langage dans les blocs de code
            - Signale les risques de sécurité, performance, compatibilité
            - Si plusieurs approches : explique les trade-offs clairement
            - Cite les versions des librairies utilisées dans le code
            - Adapte les solutions au stack de Gil (pas de React, pas de Gradle, pas de Kotlin sauf demande explicite)
            - En français, avec termes techniques en anglais si nécessaire
            """;

    private static final String SYSTEM_RESEARCH = """
            Tu es GilmoBrain en mode recherche approfondie.

            RÈGLES ABSOLUES :
            - Utilise web_search minimum 3 fois par réponse, sur des angles différents
            - Croise toujours plusieurs sources avant de conclure
            - Structure chaque réponse ainsi :
              1. Résumé en 2-3 phrases
              2. Détails organisés par points
              3. Sources citées avec URLs
              4. Conclusion et recommandation claire
            - Signale les contradictions entre sources
            - Indique la date des informations trouvées quand disponible
            - Toujours en français
            - Contextualise pour la Belgique si pertinent
            """;

    private record ModeParams(String systemPrompt, double temperature, int maxTokens) {}

    private ModeParams resolveParams(String mode) {
        return switch (mode != null ? mode : "conversation") {
            case "code" -> new ModeParams(SYSTEM_CODE, 0.1, 4096);
            case "research" -> new ModeParams(SYSTEM_RESEARCH, 0.2, 3000);
            default -> new ModeParams(SYSTEM_CONVERSATION, 0.3, 2048);
        };
    }

    public AgentResult run(String task, String sessionId, String mode) {
        long start = System.currentTimeMillis();
        ModeParams params = resolveParams(mode);

        List<GroqMessage> history = memoryService.getHistory(sessionId);
        List<GroqMessage> messages = new ArrayList<>(history);
        messages.add(new GroqMessage("user", task));

        List<String> toolsUsed = new ArrayList<>();
        int iterations = 0;

        for (int i = 0; i < 10; i++) {
            iterations++;
            GroqResponse response = groqClient.chatWithTools(params.systemPrompt(), messages, toolsRegistry.getGroqTools(), params.temperature(), params.maxTokens());

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
