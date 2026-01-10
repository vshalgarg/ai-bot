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

    private static final String TENANT = "default_tenant";
    private static final String DATABASE = "default_database";
    private static final String COLLECTION = "pdf_collection";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // Create collection (idempotent)
    public void createCollection() throws Exception {
        Map<String, String> body = Map.of(
                "tenant", TENANT,
                "database", DATABASE,
                "name", COLLECTION
        );
        post("http://chroma:8000/api/v2/collections/create", body);
    }

    // Store texts + embeddings
    public void store(List<String> texts, List<List<Double>> embeddings) throws Exception {
        Map<String, Object> body = Map.of(
                "tenant", TENANT,
                "database", DATABASE,
                "documents", texts,
                "embeddings", embeddings,
                "ids", IntStream.range(0, texts.size())
                        .mapToObj(i -> UUID.randomUUID().toString()).toList()
        );

        post("http://chroma:8000/api/v2/collections/" + COLLECTION + "/add", body);
    }

    // Query by embedding
    public List<String> query(List<Double> queryEmbedding, int nResults) throws Exception {
        Map<String, Object> body = Map.of(
                "tenant", TENANT,
                "database", DATABASE,
                "query_embeddings", List.of(queryEmbedding),
                "n_results", nResults
        );

        String res = post("http://chroma:8000/api/v2/collections/" + COLLECTION + "/query", body);
        Map<?, ?> map = MAPPER.readValue(res, Map.class);
        // extract documents
        List<?> docs = (List<?>) ((List<?>) map.get("documents")).get(0);
        return docs.stream().map(Object::toString).toList();
    }

    // Generic POST helper
    private String post(String url, Object body) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new RuntimeException("Chroma request failed: " + response.statusCode() + " - " + response.body());
        return response.body();
    }
}
