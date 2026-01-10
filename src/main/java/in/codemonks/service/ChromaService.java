package in.codemonks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ChromaService {

    private static final String BASE_URL = "http://localhost:8000"; // classic Chroma
    private static final String COLLECTION_NAME = "pdf_collection";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // Store chunks + embeddings
    public void store(List<String> texts, List<List<Double>> embeddings) throws Exception {
        Map<String, Object> body = Map.of(
                "documents", texts,
                "embeddings", embeddings,
                "ids", IntStream.range(0, texts.size())
                        .mapToObj(i -> UUID.randomUUID().toString()).toList()
        );

        post(BASE_URL + "/collections/" + COLLECTION_NAME + "/add", body);
    }

    // Search by embedding
    public List<String> search(List<Double> embedding) throws Exception {
        Map<String, Object> body = Map.of("embedding", embedding);
        String res = post(BASE_URL + "/collections/" + COLLECTION_NAME + "/query", body);

        return MAPPER.readTree(res)
                .get("documents").get(0)
                .findValuesAsText("");
    }

    private String post(String url, Object body) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Chroma request failed: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }
}
