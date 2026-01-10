package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ChromaService {

    private static final String COLLECTION_NAME = "pdf_collection";
    private static final String TENANT = "default_tenant";
    private static final String DATABASE = "default_database";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Stores documents with their embeddings into Chroma collection
     */
    public void store(List<String> texts, List<List<Double>> embeddings) throws Exception {
        if (texts.isEmpty() || embeddings.isEmpty() || texts.size() != embeddings.size()) return;

        List<String> ids = IntStream.range(0, texts.size())
                .mapToObj(i -> "doc-" + i + "-" + System.currentTimeMillis())
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "tenant", TENANT,
                "database", DATABASE,
                "documents", texts,
                "embeddings", embeddings,
                "ids", ids
        );

        String url = String.format("http://chroma:8000/api/v2/collections/%s/add", COLLECTION_NAME);

        post(url, body);
    }

    /**
     * Queries Chroma collection using embedding
     */
    public List<String> search(List<Double> queryEmbedding, int nResults) throws Exception {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) return List.of();

        Map<String, Object> body = Map.of(
                "tenant", TENANT,
                "database", DATABASE,
                "query_embeddings", List.of(queryEmbedding),
                "n_results", nResults
        );

        String url = String.format("http://chroma:8000/api/v2/collections/%s/query", COLLECTION_NAME);
        String res = post(url, body);

        JsonNode root = MAPPER.readTree(res);
        JsonNode docsNode = root.get("documents");
        if (docsNode == null || !docsNode.isArray() || docsNode.size() == 0) return List.of();

        JsonNode firstDocArray = docsNode.get(0);
        return firstDocArray.findValuesAsText(""); // returns list of strings
    }

    /**
     * Generic POST helper
     */
    private String post(String url, Map<String, Object> body) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new RuntimeException("Chroma request failed: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }
}
