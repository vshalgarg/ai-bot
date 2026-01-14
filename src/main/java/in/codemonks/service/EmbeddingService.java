package in.codemonks.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.embed-model}")
    private String embedModel;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public List<Double> embed(String text) {
        try {
            // IMPORTANT: embeddings endpoint, NOT chat
            Map<String, Object> body = Map.of(
                    "model", embedModel,
                    "input", text
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/v1/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            MAPPER.writeValueAsString(body),
                            StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = MAPPER.readTree(response.body());

            JsonNode embeddingNode = root
                    .path("data")
                    .get(0)
                    .path("embedding");

            if (!embeddingNode.isArray()) {
                throw new RuntimeException("Invalid embedding response: " + response.body());
            }

            List<Double> vector = new ArrayList<>();
            for (JsonNode n : embeddingNode) {
                vector.add(n.asDouble());
            }

            // LOG ONCE DURING STARTUP
            // System.out.println("Embedding size = " + vector.size());

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }
}
