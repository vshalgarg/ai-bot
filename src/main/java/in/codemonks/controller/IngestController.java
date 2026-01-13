package in.codemonks.controller;

import in.codemonks.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai-bot/api")
public class IngestController {

    private final PdfService pdfService;
    private final ChunkService chunkService;
    private final EmbeddingService embeddingService;
    private final VectorService vectorService;

    public IngestController(PdfService pdfService, ChunkService chunkService,
                            EmbeddingService embeddingService, VectorService vectorService) {
        this.pdfService = pdfService;
        this.chunkService = chunkService;
        this.embeddingService = embeddingService;
        this.vectorService = vectorService;
    }

    @PostMapping("/v1/ingest")
    public String ingest(@RequestParam MultipartFile file) throws Exception {
        vectorService.createCollection(); // ensure collection exists

        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);

        String text = pdfService.extractText(temp);
        List<String> chunks = chunkService.chunk(text);

        List<List<Double>> embeddings = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (String c : chunks) {
            embeddings.add(embeddingService.embed(c));
            ids.add(UUID.randomUUID().toString());
        }
        if (chunks.isEmpty()) {
            throw new RuntimeException("No chunks extracted from PDF");
        }
        vectorService.store(chunks, embeddings, ids);
        return "PDF ingested successfully";
    }
}
