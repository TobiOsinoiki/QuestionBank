package com.tobi.qbank.controller;

import com.tobi.qbank.entity.User;
import com.tobi.qbank.service.GmailEmailService;
import com.tobi.qbank.service.OtpService;

import com.tobi.qbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private OtpService otpService;
    @Autowired private GmailEmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String email    = body.get("email");
        String password = body.get("password");

        if (fullName == null || fullName.isBlank() ||
            email    == null || email.isBlank()    ||
            password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "All fields are required."));
        }

        if (userService.emailExists(email)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Email already registered."));
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        userService.saveUser(user);

        try {
            sendOtp(email);
            return ResponseEntity.ok(Map.of("message", "Registration successful. Check your email for OTP."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Registration failed. Could not send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty() ||
            !userService.checkPassword(password, optionalUser.get().getPassword())) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "Invalid email or password."));
        }

        User user = optionalUser.get();

        if (!user.isEmailVerified()) {
            try {
                sendOtp(email);
                return ResponseEntity.ok(Map.of(
                	    "requiresOtp", true,
                	    "message",     "Please verify your email. OTP sent.",
                	    "userId",      user.getId(),
                	    "fullName",    user.getFullName(),
                	    "email",       user.getEmail(),
                	    "role",        user.getRole()
                	));
            } catch (Exception e) {
                return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to send verification OTP: " + e.getMessage()));
            }
        }
        
        
        String safeRole = (user.getRole() == null || user.getRole().isBlank())
        	    ? "ROLE_USER" : user.getRole();
        	String token = user.getId() + "|" + safeRole;
        	return ResponseEntity.ok(Map.of(
        	    "token",    token,
        	    "userId",   user.getId(),
        	    "fullName", user.getFullName(),
        	    "email",    user.getEmail(),
        	    "role",     safeRole
        	));
      
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }
        if (userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not found."));
        }
        try {
            sendOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully to your email."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");

        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Email and OTP are required."));
        }

        if (!otpService.verifyOtp(email, otp)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid or expired OTP."));
        }

        userService.findByEmail(email).ifPresent(user -> {
            user.setEmailVerified(true);
            userService.updateUser(user, false);
        });

        return ResponseEntity.ok(Map.of("message", "OTP verified successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }
        if (userService.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not found."));
        }
        try {
            sendOtp(email);
            return ResponseEntity.ok(Map.of("message", "New OTP sent successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email       = body.get("email");
        String newPassword = body.get("newPassword");

        if (email == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Email and new password are required."));
        }

        Optional<User> opt = userService.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not found."));
        }

        User user = opt.get();
        user.setPassword(newPassword);
        userService.updateUser(user, true);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    private void sendOtp(String email) {
        String otp = otpService.generateOtp();
        otpService.saveOtp(email, otp);
        emailService.sendOtpEmail(email, otp);
    }
}
