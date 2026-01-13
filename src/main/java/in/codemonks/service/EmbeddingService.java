package in.codemonks.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.embed-model}")
    private String embedModel;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Double> embed(String text) {
        try {
            Map<String, Object> body = Map.of(
                    "model", embedModel,
                    "input", List.of(text)
            );

            String res = HttpUtils.postJson(
                    ollamaUrl + "/v1/embeddings",
                    MAPPER.writeValueAsString(body)
            );

            JsonNode root = MAPPER.readTree(res);
            return MAPPER.convertValue(
                    root.get("data").get(0).get("embedding"),
                    new TypeReference<List<Double>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Ollama embedding failed: " + e.getMessage(), e);
        }
    }
}
