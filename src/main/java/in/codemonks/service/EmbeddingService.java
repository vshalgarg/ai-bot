package in.codemonks.service;

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

    public List<Double> embed(String text) {
        try {
            Map<String, Object> body = Map.of(
                    "model", embedModel,
                    "text", text
            );

            Map<?, ?> res = HttpUtils.postJson(ollamaUrl + "/embed", body);
            // Response format: {"embedding": [0.1, 0.2, ...]}
            return (List<Double>) res.get("embedding");
        } catch (Exception e) {
            throw new RuntimeException("Ollama embedding failed: " + e.getMessage(), e);
        }
    }

}