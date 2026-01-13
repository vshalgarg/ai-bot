package in.codemonks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ai-bot")
public class UploadPageController {

    @GetMapping("/upload")
    public String upload() {
        return "forward:/upload/index.html";
    }
}

