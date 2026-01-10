package in.codemonks.service;

import in.codemonks.util.HttpUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChromaService {

    private static final String CHROMA_HOST = "http://chroma:8000";

    private static final String COLLECTION = "pdf_collection";

    public void store(List<String> documents, List<List<Double>> embeddings, List<String> ids) {
        try {
            Map<String, Object> body = Map.of(
                    "documents", documents,
                    "embeddings", embeddings,
                    "ids", ids
            );
            HttpUtils.post(CHROMA_HOST + "/collection/" + COLLECTION + "/add", body);
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

            Map<?, ?> res = HttpUtils.postJson(CHROMA_HOST + "/collection/" + COLLECTION + "/query", body);

            // Chroma v0 returns { "ids": [...], "documents": [...], "distances": [...] }
            List<String> documents = (List<String>) ((List<?>) res.get("documents")).get(0);
            return documents;
        } catch (Exception e) {
            throw new RuntimeException("Chroma query failed: " + e.getMessage(), e);
        }
    }
}
