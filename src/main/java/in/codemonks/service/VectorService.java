package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void store(List<String> texts, List<List<Double>> vectors) throws Exception {
        String url = vectorDbUrl + "/collections/" + tenantCollectionProperties.getCollectionNameForCurrentTenant() + "/points?wait=true";

        List<Map<String, Object>> points = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            points.add(Map.of(
                    "id", UUID.randomUUID().toString(),
                    "vector", vectors.get(i),
                    "payload", Map.of("text", texts.get(i))
            ));
        }
        String body = MAPPER.writeValueAsString(Map.of("points", points));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("vector db store response: " + response.body());
    }


    // query embeddings
    public List<String> query(List<Double> queryVector, int nResults) throws Exception {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("queryVector is null or empty");
        }

        String url = vectorDbUrl + "/collections/" + tenantCollectionProperties.getCollectionNameForCurrentTenant() + "/points/search";
        Map<String, Object> body = Map.of(
                "vector", queryVector,
                "limit", nResults,
                "with_payload", true
        );


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(
                        MAPPER.writeValueAsString(body)))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // parse response
        JsonNode root = MAPPER.readTree(response.body());
        List<String> results = new ArrayList<>();
        if (root.has("result")) {
            for (JsonNode point : root.get("result")) {
                results.add(point.get("payload").get("text").asText());
            }
        }
        return results;
    }

}

