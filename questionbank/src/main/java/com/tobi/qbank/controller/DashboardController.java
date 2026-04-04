package com.tobi.qbank.controller;

import com.tobi.qbank.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
       
        User user = (User) session.getAttribute("user");
        if (user == null) {
           
            return "redirect:/login";
        }
        model.addAttribute("username", user.getUsername());       
        return "dashboard";
    }
}