package com.project.skin_me.controller;

import com.project.skin_me.service.chatAI.GeminiService;
import com.project.skin_me.service.product.ProductService;
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

        var products = productService.getAllProducts();

        // 2. Convert to Markdown table
        String markdownTable = productService.toMarkdownTable(products);

        // 3. Build AI prompt
        String prompt = """
                You are a helpful skincare assistant for SkinMe.
                Use ONLY the product data below (Markdown table) to answer.
                Do not invent products.

                PRODUCT CATALOG:
                %s

                USER QUESTION: %s

                Answer clearly and professionally. If no match, say: "I couldn't find a matching product."
                """.formatted(markdownTable, userQuestion);

        return geminiService.askGemini(prompt);
    }
}