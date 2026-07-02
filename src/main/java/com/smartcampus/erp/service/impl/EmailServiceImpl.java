package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email via SMTP. Falling back to console logging. Reason: {}", e.getMessage());
            log.info("============== FALLBACK EMAIL LOG ==============");
            log.info("TO: {}", to);
            log.info("SUBJECT: {}", subject);
            log.info("BODY:\n{}", body);
            log.info("================================================");
        }
    }
}
