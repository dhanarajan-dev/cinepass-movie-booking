package com.moviebooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String root(HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USER_KEY) != null) {
            return "redirect:/home.html";
        }
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USER_KEY) != null) {
            return "redirect:/home.html";
        }
        return "forward:/login.html";
    }

    @GetMapping("/home")
    public String home(HttpSession session) {
        if (session == null || session.getAttribute(AuthController.SESSION_USER_KEY) == null) {
            return "redirect:/login.html";
        }
        return "forward:/home.html";
    }
}
