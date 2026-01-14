package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.model.QueryIntent;
import in.codemonks.util.HttpUtils;
import in.codemonks.util.OllamaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    @Autowired
    IntentService intentService;
    @Autowired QueryExpansionService expansionService;
    @Autowired EmbeddingService embeddingService;
    @Autowired VectorService vectorService;
    @Autowired RerankService rerankService;

    @Value("${ollama.chat-model}")
    private String model;

    @Value("${ollama.base-url}")
    private String baseUrl;

    public String ask(String question) throws Exception {

        QueryIntent intent = intentService.analyze(question);

        List<String> queries = expansionService.expand(intent);

        List<String> candidates = new ArrayList<>();
        for (String q : queries) {
            candidates.addAll(
                    vectorService.query(embeddingService.embed(q), 10)
            );
        }

        if (candidates.isEmpty()) {
            return "Not found in document";
        }

        List<String> contextChunks =
                rerankService.rerank(question, candidates);

        return answer(question, contextChunks);
    }

    private String answer(String question, List<String> chunks) throws Exception {

        String context = String.join("\n\n", chunks);

        String prompt = """
        Answer ONLY using the context below.
        If the answer is not present, say exactly:
        Not found in document.

        Context:
        %s

        Question:
        %s
        """.formatted(context, question);

        return OllamaUtils.chat(prompt,
                model, baseUrl).trim();
    }
}