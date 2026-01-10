package in.codemonks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired private ChromaService chromaService;
    @Autowired private EmbeddingService embeddingService;

    public String chat(String question) throws Exception {
        // 1️⃣ Get query embedding
        List<Double> embedding = embeddingService.embed(question);

        // 2️⃣ Query Chroma
        List<String> docs = chromaService.query(embedding, 3);

        // 3️⃣ Build prompt
        String context = String.join("\n", docs);
        String prompt = question + "\nContext:\n" + context;

        // 4️⃣ Generate answer via Ollama
        return embeddingService.generate(prompt);
    }
}
