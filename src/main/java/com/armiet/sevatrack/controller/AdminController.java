package com.armiet.sevatrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @GetMapping("/admin/login")
    public String showAdminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (username.equals("admin") && password.equals("admin123")) {

            // Store admin login status in the session
            session.setAttribute("adminLoggedIn", true);

            return "redirect:/admin-dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid username or password!"
        );

        return "admin-login";
    }

    @GetMapping("/admin-dashboard")
    public String showAdminDashboard(HttpSession session) {

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        return "admin-dashboard";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {

        // Remove the admin login session
        session.removeAttribute("adminLoggedIn");

        return "redirect:/admin/login";
    }
}