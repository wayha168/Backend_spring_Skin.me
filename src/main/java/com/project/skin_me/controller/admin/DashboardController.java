package com.project.skin_me.controller.admin;

<<<<<<< HEAD
import org.springframework.stereotype.Controller;
=======
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
>>>>>>> 3c27533a11c0c2b73bef8da34fd9e826d3b619d6

@Controller
public class DashboardController {

<<<<<<< HEAD
    // Login page is handled by PageController
    // No duplicate mappings here to avoid ambiguous mapping errors
}
=======
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
>>>>>>> 3c27533a11c0c2b73bef8da34fd9e826d3b619d6
