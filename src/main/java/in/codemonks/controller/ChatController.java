package in.codemonks.controller;

import in.codemonks.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<String> chat(@RequestParam String query) {
        return chatService.query(query, 3); // top 3 results
    }
}
