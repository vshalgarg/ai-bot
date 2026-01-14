package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    // === HARD SAFETY LIMITS ===
    private static final int MAX_TEXT_LENGTH = 2_000_000; // 2MB
    private static final int MAX_CHUNKS = 3_000;

    // Semantic chunking
    private static final int TARGET_CHUNK_SIZE = 600;
    private static final int MIN_CHUNK_SIZE = 120;

    public List<String> chunk(String rawText) {

        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }

        // 1️⃣ Hard truncate to protect heap
        String text = rawText.length() > MAX_TEXT_LENGTH
                ? rawText.substring(0, MAX_TEXT_LENGTH)
                : rawText;

        // 2️⃣ Normalize text
        text = normalize(text);

        // 3️⃣ Split by paragraphs (BEST for PDFs)
        String[] paragraphs = text.split("\\n{2,}");

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {

            para = para.trim();

            // Skip garbage
            if (para.length() < 40) continue;
            if (isNoise(para)) continue;

            if (current.length() + para.length() <= TARGET_CHUNK_SIZE) {
                current.append(para).append(" ");
            } else {
                // Flush
                if (current.length() >= MIN_CHUNK_SIZE) {
                    chunks.add(current.toString().trim());
                }
                current.setLength(0);
                current.append(para).append(" ");
            }

            // HARD STOP
            if (chunks.size() >= MAX_CHUNKS) break;
        }

        // Flush remaining
        if (current.length() >= MIN_CHUNK_SIZE && chunks.size() < MAX_CHUNKS) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }

    private String normalize(String text) {
        return text
                .replaceAll("\\r", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private boolean isNoise(String para) {
        // Page numbers, headers, footers
        if (para.matches("^page \\d+.*")) return true;
        if (para.matches("^\\d+$")) return true;
        if (para.length() < 40) return true;

        // Too many symbols = garbage
        long letters = para.chars().filter(Character::isLetter).count();
        return letters < para.length() * 0.4;
    }
}