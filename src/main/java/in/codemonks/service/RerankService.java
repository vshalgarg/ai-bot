package in.codemonks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.OllamaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RerankService {

    @Value("${ollama.chat-model}")
    private String model;

    @Value("${ollama.base-url}")
    private String baseUrl;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<String> rerank(String question, List<String> chunks) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            sb.append(i + 1).append(". ").append(chunks.get(i)).append("\n");
        }

        String prompt = """
        Select the most relevant snippets for the question.
        Return ONLY a JSON array of numbers (max 3).

        Question:
        %s

        Snippets:
        %s
        """.formatted(question, sb);

        try {
            String response = OllamaUtils.chat(prompt, model, baseUrl);
            int[] idx = MAPPER.readValue(response, int[].class);

            List<String> selected = new ArrayList<>();
            for (int i : idx) {
                if (i > 0 && i <= chunks.size()) {
                    selected.add(chunks.get(i - 1));
                }
            }
            return selected;
        } catch (Exception e) {
            return chunks.stream().limit(3).toList();
        }
    }
}
