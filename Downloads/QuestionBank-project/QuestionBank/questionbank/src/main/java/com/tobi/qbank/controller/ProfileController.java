package com.tobi.qbank.controller;

import com.tobi.qbank.entity.User;
import com.tobi.qbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        return userService.findById(userId)
            .map(user -> ResponseEntity.ok(Map.<String, Object>of(
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {

        Optional<User> opt = userService.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = opt.get();
        String fullName    = body.get("fullName");
        String newPassword = body.get("newPassword");
        String oldPassword = body.get("oldPassword");

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }

        if (newPassword != null && !newPassword.isBlank()) {
            if (oldPassword == null || !userService.checkPassword(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect."));
            }
            user.setPassword(newPassword); // UserService.updateUser will hash it
        }

        userService.updateUser(user, newPassword != null && !newPassword.isBlank());

        return ResponseEntity.ok(Map.of(
            "message", "Profile updated successfully.",
            "user", Map.of(
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail()
            )
        ));
    }
}
