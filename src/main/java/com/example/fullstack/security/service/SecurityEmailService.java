package com.example.fullstack.security.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SecurityEmailService {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    public SecurityEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(String to, String otp) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setTo(to);
            helper.setSubject("Pizza Point Admin OTP Verification");

            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("TIMESTAMP" , LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String html = templateEngine.process("email/otp-email.html",context);

            helper.setText(html , true);
            mailSender.send(message);


        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }

    }

    public void resetPasswordEmail(String to, String url) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            Context context = new Context();
            context.setVariable("url", url);
            String html = templateEngine.process("email/reset-password-email.html",context);
            helper.setText(html , true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
