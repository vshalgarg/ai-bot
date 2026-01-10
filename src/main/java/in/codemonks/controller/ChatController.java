package in.codemonks.controller;

import in.codemonks.service.ChromaService;
import in.codemonks.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired private EmbeddingService embeddingService;
    @Autowired private ChromaService chromaService;

    @PostMapping("/chat")
    public String chat(@RequestParam String question) throws Exception {
        // Embed query
        List<Double> queryEmbedding = embeddingService.embed(question);

        // Query Chroma
        List<String> docs = chromaService.query(queryEmbedding, 3);

        // Generate answer using Ollama
        String context = String.join("\n", docs);
        String prompt = question + "\nContext:\n" + context;

        return embeddingService.generate(prompt);
    }
}
