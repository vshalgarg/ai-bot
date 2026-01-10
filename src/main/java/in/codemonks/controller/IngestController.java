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
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);

        String text = pdfService.extractText(temp);
        List<String> chunks = chunkService.chunk(text);

        List<List<Double>> embeddings = new ArrayList<>();
        for (String c : chunks) embeddings.add(embeddingService.embed(c));

        chromaService.store(chunks, embeddings);
        return "PDF ingested successfully";
    }
}
