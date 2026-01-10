package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.chat-model}")
    private String chatModel;

    private final EmbeddingService embeddingService;
    private final VectorService vectorService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ChatService(EmbeddingService embeddingService,
                       VectorService vectorService) {
        this.embeddingService = embeddingService;
        this.vectorService = vectorService;
    }

    public String query(String question) throws Exception {

        // 1️⃣ Embed the query
        List<Double> queryVector = embeddingService.embed(question);

        // 2️⃣ Retrieve relevant chunks
        List<String> chunks = vectorService.query(queryVector, 5);

        if (chunks.isEmpty()) {
            return "Not found in document";
        }

        // 3️⃣ Build context
        String context = String.join("\n\n", chunks);

        // 4️⃣ Strict prompt (VERY IMPORTANT)
        String prompt = """
        You are an assistant answering questions ONLY from the provided context.

        Rules:
        - Use ONLY the context below.
        - If the answer is not explicitly mentioned, say: "Not found in document".
        - Answer in ONE sentence.
        - Do not add explanations.

        Context:
        %s

        Question:
        %s

        Answer:
        """.formatted(context, question);

        // 5️⃣ Call Ollama
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        String response = HttpUtils.postJson(
                ollamaUrl + "/v1/chat/completions",
                MAPPER.writeValueAsString(body)
        );

        // 6️⃣ Parse answer
        JsonNode root = MAPPER.readTree(response);
        return root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();
    }
}