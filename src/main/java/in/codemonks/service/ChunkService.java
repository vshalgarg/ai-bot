package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int MAX_CHARS = 800;
    private static final int OVERLAP = 150;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        text = text.replaceAll("\\r", "");
        String[] paragraphs = text.split("\\n\\n+");

        StringBuilder current = new StringBuilder();

        for (String p : paragraphs) {
            if (current.length() + p.length() > MAX_CHARS) {
                chunks.add(current.toString().trim());

                // overlap
                String overlapText = current.substring(
                        Math.max(0, current.length() - OVERLAP)
                );
                current.setLength(0);
                current.append(overlapText).append("\n\n");
            }
            current.append(p).append("\n\n");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}