package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    // Split text into chunks (simple fixed-size)
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int chunkSize = 500;
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
        }
        return chunks;
    }
}
