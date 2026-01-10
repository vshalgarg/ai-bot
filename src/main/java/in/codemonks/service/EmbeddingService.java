package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.embed-model}")
    private String embedModel;

    public List<Double> embed(String text) {
        Map<String, Object> payload = Map.of(
                "model", embedModel,
                "prompt", text
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                ollamaUrl + "/api/embeddings",
                payload,
                String.class
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            // ✅ Fix: get "embedding" instead of "embeddings"
            JsonNode embeddingNode = root.get("embedding");
            if (embeddingNode == null || !embeddingNode.isArray()) {
                throw new IllegalStateException("Invalid embedding response from Ollama: " + root);
            }

            List<Double> embedding = new ArrayList<>();
            for (JsonNode n : embeddingNode) {
                embedding.add(n.asDouble());
            }

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse embedding", e);
        }
    }

    public String generate(String prompt) throws Exception {
        String json = """
        {
          "model": "llama3",
          "prompt": "%s"
        }
        """.formatted(prompt.replace("\"", ""));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl + "/api/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res =
                HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readTree(res.body()).get("response").asText();
    }
}


