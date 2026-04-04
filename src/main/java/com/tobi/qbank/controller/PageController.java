package com.tobi.qbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/")
	public String root() {
		return "redirect:/home.html";
	}

	@GetMapping("/home.html")
	public String home() {
		return "home";
	}

	@GetMapping("/login.html")
	public String login() {
		return "login";
	}

	@GetMapping("/register.html")
	public String register() {
		return "register";
	}

	@GetMapping("/dashboard.html")
	public String dashboard() {
		return "dashboard";
	}

	@GetMapping("/quiz.html")
	public String quiz() {
		return "quiz";
	}

	@GetMapping("/result.html")
	public String result() {
		return "result";
	}

	@GetMapping("/forgot-password.html")
	public String forgotPassword() {
		return "forgot-password";
	}

	@GetMapping("/otp.html")
	public String otp() {
		return "otp";
	}
}
