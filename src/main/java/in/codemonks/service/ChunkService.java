package in.codemonks.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int size = 800;
        for (int i = 0; i < text.length(); i += size) {
            chunks.add(text.substring(i, Math.min(text.length(), i + size)));
        }
        return chunks;
    }
}
