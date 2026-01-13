package in.codemonks.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QueryExpansionService {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.chat-model}")
    private String model;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<String> expand(String question) {
        try {
            String prompt = """
            You are a JSON API.

            TASK:
            Rewrite the question into alternative phrases that may appear in a document.

            RULES:
            - Output MUST be valid JSON
            - Output MUST be a JSON array of strings
            - NO explanation
            - NO markdown
            - NO extra text

            Example output:
            ["phrase one", "phrase two"]

            Question:
            %s
            """.formatted(question);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0
            );

            String res = HttpUtils.postJson(
                    ollamaUrl + "/v1/chat/completions",
                    MAPPER.writeValueAsString(body)
            );

            String content = MAPPER.readTree(res)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            // 🔐 HARD SAFETY: extract JSON array if model adds text
            String json = extractJsonArray(content);

            return MAPPER.readValue(json, new TypeReference<List<String>>() {});

        } catch (Exception e) {
            // 🚑 FAIL SAFE — do NOT break chat
            return List.of(question);
        }
    }

    /**
     * Extracts first JSON array found in text.
     */
    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new RuntimeException("No JSON array found");
    }
}