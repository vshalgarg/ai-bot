package in.codemonks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired EmbeddingService embeddingService;
    @Autowired ChromaService chromaService;

    public String chat(String question) throws Exception {
        List<Double> qEmb = embeddingService.embed(question);
        List<String> docs = chromaService.search(qEmb, 2);

        String context = String.join("\n", docs);

        String prompt = """
        You are an assistant.
        Answer ONLY from this context:

        %s

        Question:
        %s
        """.formatted(context, question);

        return embeddingService.generate(prompt);
    }
}
