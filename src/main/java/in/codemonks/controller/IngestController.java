package in.codemonks.controller;

import in.codemonks.service.ChromaService;
import in.codemonks.service.ChunkService;
import in.codemonks.service.EmbeddingService;
import in.codemonks.service.PdfService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class IngestController {

    private final EmbeddingService embeddingService;
    private final ChromaService chromaService;
    private final ChunkService chunkService;

    public IngestController(EmbeddingService embeddingService,
                            ChromaService chromaService,
                            ChunkService chunkService) {
        this.embeddingService = embeddingService;
        this.chromaService = chromaService;
        this.chunkService = chunkService;
    }

    @PostMapping("/ingest")
    public String ingest(@RequestParam MultipartFile file) throws Exception {
        // Save temp file
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);

        // Extract text and chunk
        String text = PdfService.extractText(temp);
        List<String> chunks = chunkService.chunk(text);

        // Generate embeddings
        List<List<Double>> embeddings = new ArrayList<>();
        for (String c : chunks) embeddings.add(embeddingService.embed(c));

        // Generate IDs
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) ids.add(UUID.randomUUID().toString());

        // Store in Chroma
        chromaService.store(chunks, embeddings, ids);

        return "PDF ingested successfully";
    }
}