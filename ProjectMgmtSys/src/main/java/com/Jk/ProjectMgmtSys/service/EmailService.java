package com.Jk.ProjectMgmtSys.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // ✅ You can move this to application.properties for flexibility
    private static final String FROM_EMAIL = "jaygunjawale1010@gmail.com";

    /**
     * ✅ Send plain text email
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }

    /**
     * ✅ Send raw HTML email (manual HTML content)
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Enable HTML

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // Consider using a logger
        }
    }

    /**
     * ✅ Send HTML email using Thymeleaf template
     * Template path: src/main/resources/templates/emails/notification-email.html
     */
    public void sendNotificationEmail(String to, String subject, Map<String, Object> model) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            // Prepare Thymeleaf template context
            Context context = new Context();
            context.setVariables(model);

            // Process the HTML content using Thymeleaf
            String htmlContent = templateEngine.process("emails/notification-email", context);

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace(); // Consider replacing with proper logging
        }
    }

    /**
     * ✅ Send HTML email with file attachment
     */
    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody, File attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(attachment.getName(), attachment);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // Consider using a logger
        }
    }
}
