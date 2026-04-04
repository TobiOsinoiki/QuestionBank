package com.tobi.qbank.controller;

import com.tobi.qbank.entity.User;
import com.tobi.qbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username already taken!");
            return "register";
        }

        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Raw password — hashing is handled in UserService.saveUser()

        userService.saveUser(user);

        return "redirect:/login";
    }
}
