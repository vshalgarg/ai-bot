package in.codemonks.controller;

import in.codemonks.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class IngestController {

    private final PdfService pdfService;
    private final ChunkService chunkService;
    private final EmbeddingService embeddingService;
    private final ChromaService chromaService;

    public IngestController(PdfService pdfService, ChunkService chunkService,
                            EmbeddingService embeddingService, ChromaService chromaService) {
        this.pdfService = pdfService;
        this.chunkService = chunkService;
        this.embeddingService = embeddingService;
        this.chromaService = chromaService;
    }

    @PostMapping("/ingest")
    public String ingest(@RequestParam MultipartFile file) throws Exception {
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);

        String text = pdfService.extractText(temp);
        List<String> chunks = chunkService.chunk(text);

        List<List<Double>> embeddings = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (String chunk : chunks) {
            embeddings.add(embeddingService.embed(chunk));
            ids.add(UUID.randomUUID().toString());
        }

        chromaService.store(chunks, embeddings, ids);

        return "PDF ingested successfully";
    }
}
