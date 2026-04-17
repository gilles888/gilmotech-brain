package be.gilmotech.brain.agent;

import be.gilmotech.brain.memory.MemoryService;
import be.gilmotech.brain.tools.ToolsRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import be.gilmotech.brain.groq.GroqTool;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentService agentService;

    @MockBean
    private MemoryService memoryService;

    @MockBean
    private ToolsRegistry toolsRegistry;

    @Test
    void healthReturnsUp() throws Exception {
        when(toolsRegistry.getGroqTools()).thenReturn(List.of(
                new GroqTool("web_search", "desc", Map.of())
        ));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.toolsAvailable").isArray());
    }

    @Test
    void agentRunReturnsMockedResponse() throws Exception {
        when(agentService.run(any(), any(), any()))
                .thenReturn(new AgentService.AgentResult("Bonjour !", "test-session", List.of(), 100L));

        AgentRequest request = new AgentRequest("Salut", "test-session", "conversation");

        mockMvc.perform(post("/api/agent/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Bonjour !"))
                .andExpect(jsonPath("$.sessionId").value("test-session"));
    }

    @Test
    void deleteMemoryReturnsDeleted() throws Exception {
        mockMvc.perform(delete("/api/agent/memory/session-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }
}
