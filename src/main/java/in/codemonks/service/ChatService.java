package in.codemonks.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ChatService {

    @Autowired EmbeddingService embeddingService;
    @Autowired VectorService vectorService;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${ollama.chat-model}")
    private String model;

    @Value("${ollama.base-url}")
    private String baseUrl;

    public ChatResponse ask(String question) throws Exception {
// 1️⃣ Intent classification
        String intent = classifyIntent(question);

        // 2️⃣ Query expansion
        List<String> expandedQueries = expandQuery(question);

        // 3️⃣ Vector retrieval
        List<VectorService.SearchResult> retrieved = new ArrayList<>();
        for (String q : expandedQueries) {
            List<Double> qVec = embeddingService.embed(q);
            retrieved.addAll(vectorService.search(qVec, 5));
        }

        // 4️⃣ Rerank
        List<String> bestChunks = rerank(question, retrieved);

        // 5️⃣ Answer extraction
        String answer = extractAnswer(question, bestChunks);

        // 6️⃣ Confidence
        double confidence = confidenceScore(bestChunks);

        return new ChatResponse(answer, intent, confidence);
    }

    // ---------- HELPERS ----------

    private String classifyIntent(String q) {
        return simpleLLM("""
            Classify the question into:
            PERSON, CONTACT, LIST, FACT, PROCESS, UNKNOWN

            Question: %s
            Return only the category.
        """.formatted(q));
    }

    private List<String> expandQuery(String q) {
        String json = simpleLLM("""
            Rewrite the question using alternative phrases.
            Return JSON array only.

            Question: %s
        """.formatted(q));

        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of(q);
        }
    }

    private List<String> rerank(String question, List<VectorService.SearchResult> chunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            sb.append(i + 1).append(". ").append(chunks.get(i).text()).append("\n");
        }

        String result = simpleLLM("""
            Select chunk numbers that directly answer the question.
            Return JSON array of numbers.

            Question: %s
            Chunks:
            %s
        """.formatted(question, sb));

        try {
            List<Integer> ids = MAPPER.readValue(result, new TypeReference<>() {});
            return ids.stream().map(i -> chunks.get(i - 1).text()).toList();
        } catch (Exception e) {
            return chunks.stream().limit(2).map(VectorService.SearchResult::text).toList();
        }
    }

    private String extractAnswer(String question, List<String> context) {
        return simpleLLM("""
            Answer ONLY using the context.
            If missing, say "Not found in document".
            One sentence.

            Context:
            %s

            Question:
            %s
        """.formatted(String.join("\n", context), question));
    }

    private double confidenceScore(List<String> chunks) {
        return Math.min(1.0, chunks.size() / 3.0);
    }

    private String simpleLLM(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return MAPPER.readTree(response.body())
                    .get("choices").get(0)
                    .get("message").get("content").asText().trim();

        } catch (Exception e) {
            throw new RuntimeException("LLM call failed", e);
        }
    }

    public record ChatResponse(String answer, String intent, double confidence) {}
}