package com.project.skin_me.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "dashboard";
    }

    @GetMapping("/product")
    @PreAuthorize("hasRole('ADMIN')")
    public String productPage(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "product";
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/views/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String categoriesPage(Model model) {
        model.addAttribute("pageTitle", "Categories Management");
        return "views/admin/categories";
    }

    @GetMapping("/views/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String productsListPage(Model model) {
        model.addAttribute("pageTitle", "Products Management");
        return "views/admin/products";
    }

    @GetMapping("/views/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String ordersListPage(Model model) {
        model.addAttribute("pageTitle", "All Orders");
        return "views/admin/orders";
    }

    @GetMapping("/views/my-orders")
    @PreAuthorize("hasRole('USER')")
    public String myOrdersListPage(Model model) {
        model.addAttribute("pageTitle", "My Orders");
        return "views/user/my-orders";
    }

    @GetMapping("/views/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public String paymentsListPage(Model model) {
        model.addAttribute("pageTitle", "Payments Record");
        return "views/admin/payments";
    }

    @GetMapping("/views/my-payments")
    @PreAuthorize("hasRole('USER')")
    public String myPaymentsListPage(Model model) {
        model.addAttribute("pageTitle", "My Payments");
        return "views/user/my-payments";
    }

}
