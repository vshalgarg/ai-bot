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

    public List<String> expand(String question) throws Exception {

        String prompt = """
        Rewrite the question into alternative phrases that may appear in a document.
        Return ONLY a JSON array of strings.

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
                .path("message").path("content").asText();

        return MAPPER.readValue(content, new TypeReference<List<String>>() {});
    }
}
