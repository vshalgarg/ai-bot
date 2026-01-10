package in.codemonks.controller;

import in.codemonks.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public List<String> chat(@RequestParam String message) throws Exception {
        return chatService.chat(message);
    }
}