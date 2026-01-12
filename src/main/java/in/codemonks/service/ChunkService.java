package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChunkService {

    private static final int CHUNK_SIZE = 500; // characters

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Normalize text
        text = text
                .replaceAll("-\\n", "")   // fix hyphen breaks
                .replaceAll("\\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(text);

        StringBuilder current = new StringBuilder();
        int start = iterator.first();

        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {

            String sentence = text.substring(start, end);

            if (current.length() + sentence.length() > CHUNK_SIZE) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }

            current.append(sentence).append(" ");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}

