package in.codemonks.controller;

import in.codemonks.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-bot/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/v1/chat")
    public String chat(@RequestParam String query) throws Exception {
        return chatService.ask(query);
    }
}
