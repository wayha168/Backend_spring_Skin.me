package com.project.skin_me.config;

import com.project.skin_me.model.User;
import com.project.skin_me.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttribute {

    private final IUserService userService;

    @ModelAttribute
    public void addCurrentUser(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                User currentUser = userService.getAuthenticatedUser();
                model.addAttribute("currentUser", currentUser);
            }
        } catch (Exception e) {
            // User not authenticated or error getting user - ignore
        }
    }
}
