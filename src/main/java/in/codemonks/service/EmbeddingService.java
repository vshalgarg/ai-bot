package in.codemonks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class EmbeddingService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Double> embed(String text) throws Exception {
        String json = """
        {
          "model": "nomic-embed-text",
          "prompt": "%s"
        }
        """.formatted(text.replace("\"", ""));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://ollama:11434/api/embeddings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res =
                HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        return MAPPER.readTree(res.body()).get("embedding")
                .findValuesAsText("").stream().map(Double::valueOf).toList();
    }

    public String generate(String prompt) throws Exception {
        String json = """
        {
          "model": "llama3",
          "prompt": "%s"
        }
        """.formatted(prompt.replace("\"", ""));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://ollama:11434/api/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res =
                HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        return MAPPER.readTree(res.body()).get("response").asText();
    }
}

