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

    public List<String> chat(String message) throws Exception {
        List<Double> embedding = embeddingService.embed(message);
        return chromaService.query(embedding, 3); // return top 3 results
    }
}