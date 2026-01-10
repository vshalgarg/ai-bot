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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void store(List<String> texts, List<List<Double>> embeddings) throws Exception {
        Map<String, Object> body = Map.of(
                "documents", texts,
                "embeddings", embeddings,
                "ids", IntStream.range(0, texts.size())
                        .mapToObj(i -> UUID.randomUUID().toString()).toList()
        );

        post("http://chroma:5000/add", body);
    }

    public List<String> search(List<Double> embedding) throws Exception {
        Map<String, Object> body = Map.of("embedding", embedding);
        String res = post("http://chroma:5000/query", body);

        return MAPPER.readTree(res)
                .get("documents").get(0)
                .findValuesAsText("");
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

