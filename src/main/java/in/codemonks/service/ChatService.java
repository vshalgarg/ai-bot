package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService {

    private final EmbeddingService embeddingService;
    private final VectorService vectorService;
    private final QueryExpansionService expansionService;

    @Value("${ollama.base-url}")
    private String ollamaUrl;

    @Value("${ollama.chat-model}")
    private String chatModel;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ChatService(
            EmbeddingService embeddingService,
            VectorService vectorService,
            QueryExpansionService expansionService
    ) {
        this.embeddingService = embeddingService;
        this.vectorService = vectorService;
        this.expansionService = expansionService;
    }

    public String ask(String question) throws Exception {

        List<String> queries = expansionService.expand(question);
        queries.add(0, question);

        Set<String> contextChunks = new LinkedHashSet<>();

        for (String q : queries) {
            List<Double> vector = embeddingService.embed(q);
            contextChunks.addAll(vectorService.query(vector, 3));
        }

        if (contextChunks.isEmpty()) {
            return "Not found in document";
        }

        String context = String.join("\n\n", contextChunks);

        String prompt = """
        Answer using ONLY the context.
        You may rephrase but NOT invent facts.
        If answer is missing, say "Not found in document".
        Answer in ONE sentence.

        Context:
        %s

        Question:
        %s

        Answer:
        """.formatted(context, question);

        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        String res = HttpUtils.postJson(
                ollamaUrl + "/v1/chat/completions",
                MAPPER.writeValueAsString(body)
        );

        return MAPPER.readTree(res)
                .path("choices").get(0)
                .path("message").path("content").asText().trim();
    }
}