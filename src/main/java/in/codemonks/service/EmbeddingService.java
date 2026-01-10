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
    private String ollamaUrl; // http://ollama:11434

    @Value("${ollama.chat-model}")
    private String chatModel; // llama-3-7b

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // -----------------------------
    // Pseudo embedding for Chroma
    // -----------------------------
    public List<Double> embed(String text) {
        double[] vec = new double[16];
        int hash = text.hashCode();
        for (int i = 0; i < vec.length; i++) vec[i] = ((hash >> (i * 2)) & 0xFF) / 255.0;
        List<Double> embedding = new ArrayList<>();
        for (double v : vec) embedding.add(v);
        return embedding;
    }

    // -----------------------------
    // Generate text via Ollama
    // -----------------------------
    public String generate(String prompt) throws Exception {
        String body = String.format("{\"model\":\"%s\",\"prompt\":\"%s\"}", chatModel, prompt.replace("\"","\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(ollamaUrl + "/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) throw new RuntimeException("Ollama generate failed: " + response.body());

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode outputNode = root.get("output");
        if (outputNode == null) return "";
        if (outputNode.isArray() && outputNode.size() > 0) return outputNode.get(0).asText();
        return outputNode.asText();
    }
}
