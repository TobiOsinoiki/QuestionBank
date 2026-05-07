package com.tobi.qbank.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class GmailEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                  <h2>QBank verification code</h2>
                  <p>Use the code below to verify your email. It expires shortly.</p>
                  <div style="font-size: 28px; font-weight: bold; letter-spacing: 4px;
                              padding: 16px; background: #f4f4f4; text-align: center;">
                    %s
                  </div>
                  <p style="color:#666; font-size: 12px;">
                    If you didn't request this, you can ignore this email.
                  </p>
                </div>
                """.formatted(otp);

            helper.setFrom(fromEmail, "QBank");
            helper.setTo(toEmail);
            helper.setSubject("Your QBank verification code");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
}