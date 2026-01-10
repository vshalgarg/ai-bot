package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.http.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorService {

    private static final String QDRANT_URL = "http://qdrant:6333";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COLLECTION = "pdf_collection3";

    // create collection if not exists
    public void createCollection() throws Exception {
        String url = QDRANT_URL + "/collections/" + COLLECTION;
        String body = "{\"vectors\":{ \"size\": 4096, \"distance\": \"Cosine\"} }"; // adjust vector_size to your embedding size
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Qdrant returns 200 if exists, 201 if created
    }

    public void store(List<String> documents, List<List<Double>> embeddings, List<String> ids) throws Exception {
        String url = QDRANT_URL + "/collections/" + COLLECTION + "/points?wait=true";

        List<Map<String, Object>> points = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            String doc = documents.get(i);
            List<Double> vector = embeddings.get(i);

            String id = ids.get(i);

            if (doc == null || vector == null || id == null) {
                continue; // skip nulls
            }
            System.out.println("vector size is:  " + vector.size());
            System.out.println("id is: " + id + ", doc is: " + doc);
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", doc);

            Map<String, Object> point = new HashMap<>();
            point.put("id", id);
            point.put("vector", vector);
            point.put("payload", payload);

            points.add(point);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("points", points);

        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Qdrant store response: " + response.body());
    }


    // query embeddings
    public List<String> query(List<Double> queryVector, int nResults) throws Exception {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("queryVector is null or empty");
        }

        String url = QDRANT_URL + "/collections/" + COLLECTION + "/points/search";

        Map<String, Object> payload = new HashMap<>();
        payload.put("vector", queryVector);
        payload.put("top", nResults);

        // Optional: filter
        Map<String, Object> filter = new HashMap<>();
        // populate filter if needed, skip if null
        if (!filter.isEmpty()) {
            payload.put("filter", filter);
        }

        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Qdrant query response: " + response.body());
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

