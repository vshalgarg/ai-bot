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

@Service
public class EmbeddingService {

    @Value("${ollama.base-url}")
    private String ollamaUrl; // e.g., http://ollama:11434

    @Value("${ollama.embed-model}")
    private String embedModel; // e.g., "llama-3-7b"

    @Value("${ollama.chat-model}")
    private String chatModel; // e.g., "llama-3-7b"

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // -----------------------------
    // Generate embedding vector
    // -----------------------------
    public List<Double> embed(String text) throws Exception {
        if (text == null || text.isEmpty()) return new ArrayList<>();

        String requestBody = String.format("{\"model\":\"%s\",\"text\":\"%s\"}",
                embedModel,
                text.replace("\"","\\\"")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(ollamaUrl + "/embeddings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama embedding failed: " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode embeddingNode = root.get("embedding");
        if (embeddingNode == null || !embeddingNode.isArray()) {
            throw new RuntimeException("Invalid embedding response: " + response.body());
        }

        List<Double> embedding = new ArrayList<>();
        for (JsonNode n : embeddingNode) {
            embedding.add(n.asDouble());
        }
        return embedding;
    }

    // -----------------------------
    // Generate text from prompt
    // -----------------------------
    public String generate(String prompt) throws Exception {
        if (prompt == null || prompt.isEmpty()) return "";

        String requestBody = String.format("{\"model\":\"%s\",\"prompt\":\"%s\"}",
                chatModel,
                prompt.replace("\"","\\\"")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(ollamaUrl + "/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama generate failed: " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode outputNode = root.get("output"); // Ollama returns generated text in "output"
        if (outputNode == null) return "";

        // In case output is array or string
        if (outputNode.isArray() && outputNode.size() > 0) {
            return outputNode.get(0).asText();
        } else {
            return outputNode.asText();
        }
    }
}
