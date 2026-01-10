package in.codemonks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codemonks.util.HttpUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChromaService {

    private static final String CHROMA_HOST = "http://chroma:8000";

    private static final String COLLECTION = "pdf_collection";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public void store(List<String> documents, List<List<Double>> embeddings, List<String> ids) {
        try {
            Map<String, Object> body = Map.of(
                    "documents", documents,
                    "embeddings", embeddings,
                    "ids", ids
            );
            HttpUtils.postJson(CHROMA_HOST + "/collections/" + COLLECTION + "/add", MAPPER.writeValueAsString(body));
        } catch (Exception e) {
            throw new RuntimeException("Chroma request failed: " + e.getMessage(), e);
        }
    }

    public List<String> query(List<Double> embedding, int nResults) {
        try {
            Map<String, Object> body = Map.of(
                    "query", List.of(embedding),
                    "n_results", nResults
            );

            String res = HttpUtils.postJson(CHROMA_HOST + "/collections/" + COLLECTION + "/query", MAPPER.writeValueAsString(body));

            JsonNode node = MAPPER.readTree(res);
            return MAPPER.convertValue(node.get("documents").get(0), MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            throw new RuntimeException("Chroma query failed: " + e.getMessage(), e);
        }
    }
}
