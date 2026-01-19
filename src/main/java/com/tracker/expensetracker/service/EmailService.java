package com.tracker.expensetracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:expense-tracker@example.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // If mail sender is not configured, log the token instead
        if (mailSender == null) {
            System.out.println("Email service not configured. Password reset token for " + toEmail + ": " + resetToken);
            System.out.println("Reset link: " + baseUrl + "/reset-password.html?token=" + resetToken);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("TrackExpense - Password Reset Request");
            
            String resetLink = baseUrl + "/reset-password.html?token=" + resetToken;
            message.setText(
                "Hello,\n\n" +
                "You requested to reset your password for your TrackExpense account.\n\n" +
                "Click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n"
            );

            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
