package in.codemonks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.http.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class VectorService {

    private static final String QDRANT_URL = "http://qdrant:6333";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COLLECTION = "pdf_collection";

    // create collection if not exists
    public void createCollection() throws Exception {
        String url = QDRANT_URL + "/collections/" + COLLECTION;
        String body = "{ \"vector_size\": 384, \"distance\": \"Cosine\" }"; // adjust vector_size to your embedding size
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Qdrant returns 200 if exists, 201 if created
    }

    // store embeddings
    public void store(List<String> documents, List<List<Double>> embeddings, List<String> ids) throws Exception {
        String url = QDRANT_URL + "/collections/" + COLLECTION + "/points?wait=true";

        // construct payload
        var points = new java.util.ArrayList<Map<String, Object>>();
        for (int i = 0; i < documents.size(); i++) {
            points.add(Map.of(
                    "id", ids.get(i),
                    "vector", embeddings.get(i),
                    "payload", Map.of("text", documents.get(i))
            ));
        }

        Map<String, Object> payload = Map.of("points", points);
        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // query embeddings
    public List<String> query(List<Double> queryVector, int n) throws Exception {
        String url = QDRANT_URL + "/collections/" + COLLECTION + "/points/search?limit=" + n;

        Map<String, Object> payload = Map.of("vector", queryVector);
        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var json = MAPPER.readTree(response.body());
        var results = new java.util.ArrayList<String>();
        for (var r : json.get("result")) {
            results.add(r.get("payload").get("text").asText());
        }
        return results;
    }
}

