package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {

    public List<String> extractPages(MultipartFile file) throws Exception {

        List<String> pages = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();

            int pageCount = document.getNumberOfPages();

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String text = stripper.getText(document)
                        .replaceAll("\\s+", " ")
                        .trim();

                if (!text.isBlank()) {
                    pages.add(text);
                }
            }
        }

        return pages;
    }
}
