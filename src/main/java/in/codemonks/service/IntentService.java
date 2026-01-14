package in.codemonks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.model.QueryIntent;
import in.codemonks.util.OllamaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentService {

    @Value("${ollama.chat-model}")
    private String model;

    @Value("${ollama.base-url}")
    private String baseUrl;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public QueryIntent analyze(String question) {

        String prompt = """
        You are an intent classifier.
        Return ONLY valid JSON.

        Allowed intents:
        CONTACT, SERVICES, CLIENTS, ABOUT, OWNER, UNKNOWN

        JSON format:
        {
          "intent": "...",
          "keywords": ["..."],
          "expectedAnswerType": "fact|list|paragraph"
        }

        Question:
        %s
        """.formatted(question);

        try {
            String response = OllamaUtils.chat(prompt, model, baseUrl);
            return MAPPER.readValue(response, QueryIntent.class);
        } catch (Exception e) {
            return new QueryIntent("UNKNOWN", List.of(question), "paragraph");
        }
    }
}
