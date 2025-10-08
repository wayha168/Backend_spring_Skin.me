package com.project.skin_me.controller;

import com.project.skin_me.request.AddProductRequest;
import com.project.skin_me.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DashboardController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Thymeleaf will look for login.html in templates/
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard"; // dashboard.html in templates/
    }
//    @PostMapping("/products/add")
//    public String addProduct(@ModelAttribute AddProductRequest request) {
//        productService.addProduct(request); // save product
//        return "redirect:/dashboard"; // refresh page
//    }
}
