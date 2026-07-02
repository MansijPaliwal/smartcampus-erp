package com.smartcampus.erp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardViewController {

    @GetMapping("/")
    public String indexView() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginView() {
        return "forward:/login.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordView() {
        return "forward:/forgot-password.html";
    }

    @GetMapping("/reset-password")
    public String resetPasswordView() {
        return "forward:/reset-password.html";
    }

    @GetMapping("/admin/users")
    public String adminUsersView() {
        return "forward:/admin-users.html";
    }

    @GetMapping("/admin/settings")
    public String adminSettingsView() {
        return "forward:/admin-settings.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "forward:/student-dashboard.html";
    }

    @GetMapping("/faculty-dashboard")
    public String facultyDashboard() {
        return "forward:/faculty-dashboard.html";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "forward:/checkout.html";
    }
}
