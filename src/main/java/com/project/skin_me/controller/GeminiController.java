package com.project.skin_me.controller;

import com.project.skin_me.service.chatAI.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/chat")
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/assistant")
    public String askGeminiAPI(@RequestBody String prompt){
        return geminiService.askGemini(prompt);
    }
}
