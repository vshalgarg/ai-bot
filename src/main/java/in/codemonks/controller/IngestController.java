package in.codemonks.controller;

import in.codemonks.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class IngestController {

    @Autowired private PdfService pdfService;
    @Autowired private ChunkService chunkService;
    @Autowired private EmbeddingService embeddingService;
    @Autowired private ChromaService chromaService;

    @PostMapping("/ingest")
    public String ingest(@RequestParam MultipartFile file) throws Exception {
        // 1️⃣ Create collection if not exists
        chromaService.createCollection();

        // 2️⃣ Extract text
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);
        String text = pdfService.extractText(temp);

        // 3️⃣ Chunk
        List<String> chunks = chunkService.chunk(text);

        // 4️⃣ Embed
        List<List<Double>> embeddings = new ArrayList<>();
        for (String c : chunks) embeddings.add(embeddingService.embed(c));

        // 5️⃣ Store in Chroma
        chromaService.store(chunks, embeddings);

        return "PDF ingested successfully!";
    }
}
