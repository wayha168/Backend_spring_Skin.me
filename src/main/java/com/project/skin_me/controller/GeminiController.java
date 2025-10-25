package com.project.skin_me.controller;

import com.project.skin_me.service.chatAI.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/chat")
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/assistant")
    public String askGeminiGet(@RequestBody String prompt){
        return geminiService.askGemini(prompt);
    }

    @PostMapping("/assistant")
    public String askGeminiAPI(@RequestBody String prompt){
        return geminiService.askGemini(prompt);
    }
}
