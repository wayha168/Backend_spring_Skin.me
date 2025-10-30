package com.project.skin_me.controller;

import com.project.skin_me.service.chatAI.GeminiService;
import com.project.skin_me.service.product.ProductService;
import com.project.skin_me.util.MarkdownCatalogLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/chat")
public class GeminiController {

    private final GeminiService geminiService;
    private final ProductService productService;

    @PostMapping("/assistant")
    public String askGeminiWithProducts(@RequestBody String userQuestion) {
        try {
            // 1. Load the **latest** markdown from disk
            String markdownTable = MarkdownCatalogLoader.load();

            // 2. Build prompt (same logic, just source changed)
            String prompt = """
                    You are a helpful skincare assistant for SkinMe.
                    Use ONLY the product data below (Markdown table) to answer.
                    Do not invent products.

                    PRODUCT CATALOG:
                    %s

                    USER QUESTION: %s

                    Answer clearly and professionally.
                    If no match, say: "I couldn't find a matching product."
                    """.formatted(markdownTable, userQuestion);

            return geminiService.askGemini(prompt);

        } catch (Exception e) {
            return "Sorry, the product catalog is temporarily unavailable. Please try again later.";
        }
    }
}