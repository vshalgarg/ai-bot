package in.codemonks.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PdfService {

    public String extractText(File pdfFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
