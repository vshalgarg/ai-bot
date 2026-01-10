package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final VectorService vectorService;
    private final EmbeddingService embeddingService;

    public ChatService(VectorService vectorService, EmbeddingService embeddingService) {
        this.vectorService = vectorService;
        this.embeddingService = embeddingService;
    }

    /**
     * Generate a response from Chroma for a user query.
     *
     * @param query The user query string.
     * @param topK  Number of top results to return.
     * @return List of matching documents.
     */
    public List<String> query(String query, int topK) throws Exception {
        // Step 1: Get embedding for the query
        List<Double> queryEmbedding = embeddingService.embed(query);

        // Step 2: Query Chroma collection
        return vectorService.query(queryEmbedding, topK);
    }
}
