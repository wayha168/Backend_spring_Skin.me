package com.project.skin_me.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {

    @GetMapping("/login-page")
    public String loginPage() {
        return "login"; // Thymeleaf will look for login.html in templates/
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard"; // dashboard.html in templates/
    }

}
