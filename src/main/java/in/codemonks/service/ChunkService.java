package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int CHUNK_SIZE = 500; // chars

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += CHUNK_SIZE) {
            chunks.add(text.substring(i, Math.min(text.length(), i + CHUNK_SIZE)));
        }
        return chunks;
    }
}
