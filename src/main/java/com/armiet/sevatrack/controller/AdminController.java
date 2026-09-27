package com.armiet.sevatrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
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
    public String showAdminDashboard(
            HttpSession session,
            HttpServletResponse response) {

        // Prevent browser/proxy caching of the admin dashboard
        disableCaching(response);

        Boolean adminLoggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (adminLoggedIn == null || !adminLoggedIn) {
            return "redirect:/admin/login";
        }

        return "admin-dashboard";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(
            HttpSession session,
            HttpServletResponse response) {

        // Completely destroy the current admin session
        session.invalidate();

        // Prevent the logout response from being cached
        disableCaching(response);

        return "redirect:/admin/login";
    }

    /**
     * Prevent the browser and intermediate proxies
     * from storing protected pages in cache.
     */
    private void disableCaching(HttpServletResponse response) {

        response.setHeader(
                "Cache-Control",
                "no-store, no-cache, must-revalidate, max-age=0"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );
    }
}