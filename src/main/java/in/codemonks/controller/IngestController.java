package in.codemonks.controller;

import in.codemonks.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/ai-bot/api")
public class IngestController {

    private final PdfService pdfService;
    private final ChunkService chunkService;
    private final EmbeddingService embeddingService;
    private final VectorService vectorService;

    private static final int EMBED_BATCH_SIZE = 16;


    public IngestController(PdfService pdfService, ChunkService chunkService,
                            EmbeddingService embeddingService, VectorService vectorService) {
        this.pdfService = pdfService;
        this.chunkService = chunkService;
        this.embeddingService = embeddingService;
        this.vectorService = vectorService;
    }

    @PostMapping("/v1/ingest")
    public ResponseEntity<?> ingest(@RequestParam MultipartFile file) throws Exception {
        vectorService.createCollection(); // ensure collection exists

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("PDF file is required");
        }

        try {
            // 1️⃣ Extract text page-wise
            List<String> pages = pdfService.extractPages(file);

            // 2️⃣ Chunk page-aware (CRITICAL)
            List<String> chunks = new ArrayList<>();
            for (int pageNo = 0; pageNo < pages.size(); pageNo++) {
                chunks.addAll(
                        chunkService.chunk(pages.get(pageNo), pageNo)
                );
            }

            if (chunks.isEmpty()) {
                return ResponseEntity.badRequest().body("No text extracted");
            }

            // 3️⃣ Generate stable IDs
            List<String> ids = IntStream.range(0, chunks.size())
                    .mapToObj(i -> UUID.randomUUID().toString())
                    .toList();

            // 4️⃣ Embed + store in batches
            for (int i = 0; i < chunks.size(); i += EMBED_BATCH_SIZE) {

                int end = Math.min(i + EMBED_BATCH_SIZE, chunks.size());

                List<String> batchChunks = chunks.subList(i, end);
                List<String> batchIds = ids.subList(i, end);

                List<List<Double>> embeddings =
                        embeddingService.embedBatch(batchChunks);

                vectorService.store(batchIds, batchChunks, embeddings);
            }

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "pages", pages.size(),
                            "chunks", chunks.size()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ingestion failed: " + e.getMessage());
        }
    }
}
