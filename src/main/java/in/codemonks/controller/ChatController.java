package in.codemonks.controller;

import in.codemonks.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) throws Exception {
        return chatService.chat(body.get("query"));
    }
}

