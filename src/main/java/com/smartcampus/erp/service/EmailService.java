package com.smartcampus.erp.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
