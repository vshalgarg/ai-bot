package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChromaService {

    @Value("${chroma.base-url}")
    private String baseUrl;

    @Value("${chroma.tenant}")
    private String tenant;

    @Value("${chroma.database}")
    private String database;

    @Value("${chroma.collection}")
    private String collection;

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Create collection (run once)
    public void createCollection() throws Exception {
        Map<String, Object> body = Map.of(
                "tenant", tenant,
                "database", database,
                "name", collection
        );
        post("/api/v2/collections/create", body);
    }

    // Store documents + embeddings
    public void store(List<String> docs, List<List<Double>> embeddings, List<String> ids) throws Exception {
        Map<String, Object> body = Map.of(
                "tenant", tenant,
                "database", database,
                "documents", docs,
                "embeddings", embeddings,
                "ids", ids
        );
        post("/api/v2/collections/" + collection + "/add", body);
    }

    // Query embeddings
    public List<String> query(List<Double> embedding, int nResults) throws Exception {
        Map<String, Object> body = Map.of(
                "tenant", tenant,
                "database", database,
                "query_embeddings", List.of(embedding),
                "n_results", nResults
        );
        String res = post("/api/v2/collections/" + collection + "/query", body);
        JsonNode node = MAPPER.readTree(res);
        if (node.has("results") && node.get("results").isArray() && node.get("results").size() > 0) {
            return MAPPER.convertValue(node.get("results").get(0).get("documents"), List.class);
        }
        return List.of();
    }

    private String post(String endpoint, Map<String, Object> body) throws Exception {
        return HttpUtils.post(baseUrl + endpoint, MAPPER.writeValueAsString(body));
    }
}