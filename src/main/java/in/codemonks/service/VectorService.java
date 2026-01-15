package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.model.VectorResult;
import in.codemonks.properties.TenantCollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.*;
import java.net.URI;
import java.util.*;

@Service
@Slf4j
public class VectorService {

    @Value("${vector-db.base-url}")
    private String vectorDbUrl;

    @Value("${vector-db.vector-size}")
    private int vectorSize;

    @Autowired
    private TenantCollectionProperties tenantCollectionProperties;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // create collection if not exists
    public void createCollection() throws Exception {
        String url = vectorDbUrl + "/collections/" + tenantCollectionProperties.getCollectionNameForCurrentTenant();
        String body = "{\"vectors\":{ \"size\": " + vectorSize + ", \"distance\": \"Cosine\"} }"; // adjust vector_size to your embedding size
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Qdrant returns 200 if exists, 201 if created
    }

    public void store(List<String> ids, List<String> texts, List<List<Double>> vectors) {
        try {
            List<Map<String, Object>> points = new ArrayList<>();

            for (int i = 0; i < texts.size(); i++) {
                points.add(Map.of(
                        "id", ids.get(i),
                        "vector", vectors.get(i),
                        "payload", Map.of(
                                "text", texts.get(i)
                        )
                ));
            }

            Map<String, Object> body = Map.of("points", points);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vectorDbUrl + "/collections/" + tenantCollectionProperties.getCollectionNameForCurrentTenant() + "/points?wait=true"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            throw new RuntimeException("Qdrant store failed", e);
        }
    }


    // query embeddings
    public List<SearchResult> search(List<Double> queryVector, int limit) {
        try {
            Map<String, Object> body = Map.of(
                    "vector", queryVector,
                    "limit", limit,
                    "with_payload", true
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vectorDbUrl + "/collections/" + tenantCollectionProperties.getCollectionNameForCurrentTenant() + "/points/search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = MAPPER.readTree(response.body());

            List<SearchResult> results = new ArrayList<>();
            for (JsonNode r : root.get("result")) {
                results.add(new SearchResult(
                        r.get("score").asDouble(),
                        r.get("payload").get("text").asText()
                ));
            }

            return results;

        } catch (Exception e) {
            throw new RuntimeException("Qdrant search failed", e);
        }
    }

    public record SearchResult(double score, String text) {}

}

