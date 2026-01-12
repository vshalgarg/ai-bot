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

        // 1️⃣ Embed query
        List<Double> queryVector = embeddingService.embed(question);

        // 2️⃣ Retrieve top 3 chunks only
        List<String> chunks = vectorService.query(queryVector, 3);

        if (chunks.isEmpty()) {
            return "Not found in document";
        }

        // 3️⃣ Build clean context
        String context = String.join("\n\n", chunks);

        // 4️⃣ STRONG extraction prompt
        String prompt = """
    You are a factual extraction assistant.

    Use ONLY the context below to answer the question.

    Instructions:
    - Identify and list the challenges students face.
    - Do NOT add information not present in the context.
    - Answer in bullet points.
    - If the answer is not present, reply exactly: Not found in document

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

        // 6️⃣ Parse response safely
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