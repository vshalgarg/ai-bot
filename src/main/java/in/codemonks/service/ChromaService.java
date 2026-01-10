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
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ChromaService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String collectionName = "pdf_collection";

    public void store(List<String> texts, List<List<Double>> embeddings) throws Exception {
        Map<String, Object> body = Map.of(
                "documents", texts,
                "embeddings", embeddings,
                "ids", IntStream.range(0, texts.size())
                        .mapToObj(i -> UUID.randomUUID().toString()).toList()
        );

        String url = "http://chroma:8000/collections/" + collectionName + "/add";
        post(url, body);
    }

    public List<String> search(List<Double> embedding) throws Exception {
        Map<String, Object> body = Map.of(
                "query", List.of(embedding),
                "n_results", 5
        );

        String url = "http://chroma:8000/collections/" + collectionName + "/query";
        String res = post(url, body);

        JsonNode json = MAPPER.readTree(res);
        JsonNode docs = json.get("documents");
        if (docs != null && docs.isArray() && docs.size() > 0) {
            return docs.get(0).findValuesAsText("");
        } else {
            return List.of();
        }
    }

    private String post(String url, Object body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        return HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString())
                .body();
    }
}

