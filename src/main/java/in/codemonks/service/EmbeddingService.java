package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Embed a single text
     */
    public List<Double> embed(String text) {
        return embedBatch(List.of(text)).get(0);
    }

    /**
     * Embed multiple texts in one call (IMPORTANT)
     */
    public List<List<Double>> embedBatch(List<String> texts) {
        try {
            Map<String, Object> body = Map.of(
                    "model", embedModel,
                    "input", texts
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/v1/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Embedding failed: " + response.body());
            }

            JsonNode root = MAPPER.readTree(response.body());
            List<List<Double>> vectors = new ArrayList<>();

            for (JsonNode item : root.get("data")) {
                List<Double> vec = new ArrayList<>(item.get("embedding").size());
                for (JsonNode v : item.get("embedding")) {
                    vec.add(v.doubleValue());
                }
                vectors.add(vec);
            }

            return vectors;

        } catch (Exception e) {
            throw new RuntimeException("Ollama embedding failed", e);
        }
    }
}
