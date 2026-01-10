package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChromaService chromaService;
    private final EmbeddingService embeddingService;

    public ChatService(ChromaService chromaService, EmbeddingService embeddingService) {
        this.chromaService = chromaService;
        this.embeddingService = embeddingService;
    }

    /**
     * Generate a response from Chroma for a user query.
     *
     * @param query The user query string.
     * @param topK  Number of top results to return.
     * @return List of matching documents.
     */
    public List<String> query(String query, int topK) {
        // Step 1: Get embedding for the query
        List<Double> queryEmbedding = embeddingService.embed(query);

        // Step 2: Query Chroma collection
        return chromaService.query(queryEmbedding, topK);
    }
}
