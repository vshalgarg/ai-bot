package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChunkService {

    // Conservative values (safe for LLMs)
    private static final int MAX_CHARS = 800;
    private static final int OVERLAP_CHARS = 150;

    public List<String> chunk(String pageText, int pageNumber) {

        List<String> chunks = new ArrayList<>();

        if (pageText == null || pageText.isBlank()) {
            return chunks;
        }

        BreakIterator sentenceIterator =
                BreakIterator.getSentenceInstance(Locale.US);

        sentenceIterator.setText(pageText);

        StringBuilder currentChunk = new StringBuilder();
        int start = sentenceIterator.first();

        for (int end = sentenceIterator.next();
             end != BreakIterator.DONE;
             start = end, end = sentenceIterator.next()) {

            String sentence = pageText.substring(start, end).trim();

            if (sentence.isEmpty()) continue;

            if (currentChunk.length() + sentence.length() > MAX_CHARS) {

                chunks.add(
                        decorate(currentChunk.toString(), pageNumber)
                );

                // Overlap
                currentChunk = new StringBuilder(
                        currentChunk.substring(
                                Math.max(0, currentChunk.length() - OVERLAP_CHARS)
                        )
                );
            }

            currentChunk.append(sentence).append(" ");
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(
                    decorate(currentChunk.toString(), pageNumber)
            );
        }

        return chunks;
    }

    private String decorate(String text, int page) {
        return "[Page " + (page + 1) + "] " + text.trim();
    }
}