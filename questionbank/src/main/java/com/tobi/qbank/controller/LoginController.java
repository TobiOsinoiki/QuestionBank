package com.tobi.qbank.controller;

import com.tobi.qbank.entity.User;
import com.tobi.qbank.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent() && userService.checkPassword(password, optionalUser.get().getPassword())) {
            session.setAttribute("user", optionalUser.get());
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid username or password!");
        return "login";
    }
}
