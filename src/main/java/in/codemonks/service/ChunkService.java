package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int MAX_CHARS = 900;
    private static final int OVERLAP = 200;

    public List<String> chunk(String text) {

        text = normalize(text);
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHARS, text.length());

            int lastPara = text.lastIndexOf("\n\n", end);
            if (lastPara > start + 300) {
                end = lastPara;
            }

            chunks.add(text.substring(start, end).trim());
            start = end - OVERLAP;
        }

        return chunks;
    }

    private String normalize(String text) {
        return text
                .replaceAll("\\s+", " ")
                .replaceAll("\\n{2,}", "\n\n")
                .trim();
    }
}