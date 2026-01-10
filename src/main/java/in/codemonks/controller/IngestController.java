package in.codemonks.controller;

import in.codemonks.service.ChromaService;
import in.codemonks.service.ChunkService;
import in.codemonks.service.EmbeddingService;
import in.codemonks.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class IngestController {

    @Autowired
    PdfService pdfService;
    @Autowired
    ChunkService chunkService;
    @Autowired
    EmbeddingService embeddingService;
    @Autowired
    ChromaService chromaService;

    @PostMapping("/ingest")
    public String ingest(@RequestParam MultipartFile file) throws Exception {
        // 1. Create temporary PDF file
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);
        temp.deleteOnExit();

        // 2. Extract text
        String text = pdfService.extractText(temp);

        // 3. Split text into chunks
        List<String> chunks = chunkService.chunk(text);

        // 4. Generate embeddings
        List<List<Double>> embeddings = new ArrayList<>();
        for (String c : chunks) {
            List<Double> emb = embeddingService.embed(c);
            if (emb != null && !emb.isEmpty()) embeddings.add(emb);
        }

        // 5. Store in Chroma collection
        chromaService.store(chunks, embeddings);

        return "PDF ingested successfully";
    }

}
