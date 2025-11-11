package com.project.skin_me.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // GET /login-page → shows your orange login page
    @GetMapping("/login-page")
    public String loginPage() {
        return "auth/login";   // → src/main/resources/templates/auth/login.html
    }

    // GET /layout/dashboard → admin dashboard
    @GetMapping("/layout/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboardPage() {
        return "/layout/dashboard";
    }
}