package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    // Ideal for Llama / nomic embeddings
    private static final int MAX_CHUNK_CHARS = 600;
    private static final int MIN_CHUNK_CHARS = 150;

    /**
     * Entry method used by your ingestion pipeline
     */
    public List<String> chunk(String rawText) {

        String text = normalize(rawText);
        return sentenceAwareChunking(text);
    }

    /**
     * Fixes PDF extraction issues:
     * - Removes broken line breaks
     * - Preserves paragraph breaks
     * - Normalizes spaces
     */
    private String normalize(String text) {

        if (text == null) return "";

        // Convert Windows line endings
        text = text.replace("\r\n", "\n");

        // Remove line breaks INSIDE sentences
        // Keeps paragraph breaks intact
        text = text.replaceAll("(?<!\\n)\\n(?!\\n)", " ");

        // Normalize multiple newlines to paragraph break
        text = text.replaceAll("\\n{2,}", "\n\n");

        // Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    /**
     * Sentence-aware chunking (CRITICAL FOR GOOD RETRIEVAL)
     */
    private List<String> sentenceAwareChunking(String text) {

        List<String> chunks = new ArrayList<>();

        // Split by sentence endings
        String[] sentences = text.split("(?<=[.!?])\\s+");

        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {

            // If adding this sentence exceeds max chunk size
            if (current.length() + sentence.length() > MAX_CHUNK_CHARS) {

                // Only add meaningful chunks
                if (current.length() >= MIN_CHUNK_CHARS) {
                    chunks.add(current.toString().trim());
                }

                current.setLength(0);
            }

            current.append(sentence).append(" ");
        }

        // Add remaining text
        if (current.length() >= MIN_CHUNK_CHARS) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}

