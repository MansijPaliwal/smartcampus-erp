package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.service.AiChatbotService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiChatbotServiceImpl implements AiChatbotService {

    private final ChatClient chatClient;

    public AiChatbotServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String getFeatureHelpResponse(String userRole, String userMessage) {
        String systemPlaybook = String.format(
            "You are the friendly, helpful internal assistant for SmartCampus ERP. " +
            "The user is authenticated as a %s. " +
            "Here is how they navigate the portal in the UI: " +
            "- Students can view their courses in the 'Current Course Enrollments' list, check exam scores in the 'Evaluation Roster', track their CGPA card, and pay invoices by clicking 'Pay Due' in the fees section. " +
            "- Faculty can select a course to record attendance in the 'Mark Attendance' tab, input marks via the 'Submit Grades' tab, and publish assignments or grade student solution uploads. " +
            "- Admins can search and register users in the 'User Management' page, view university-wide financial balances, and update settings. " +
            "Answer the user's question directly in a friendly, conversational, and encouraging tone. Use exactly 3 sentences to guide them step-by-step through the dashboard interface.",
            userRole.toUpperCase()
        );

        try {
            return chatClient.prompt()
                    .system(systemPlaybook)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (TransientAiException e) {
            log.error("AI Chatbot Error: ", e);
            return getSimulatedFallback(userRole, userMessage);
        } catch (NonTransientAiException e) {
            log.error("AI Chatbot Error: ", e);
            return getSimulatedFallback(userRole, userMessage);
        } catch (Exception e) {
            log.error("AI Chatbot Error: ", e);
            return getSimulatedFallback(userRole, userMessage);
        }
    }

    private String getSimulatedFallback(String userRole, String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        if (lowerMessage.contains("pay") || lowerMessage.contains("bill") || lowerMessage.contains("fee") || lowerMessage.contains("invoice")) {
            return "To pay your tuition bills or outstanding invoices, please navigate to the Student Dashboard and click the 'Pay Due' button in the Fees section. If you are an administrator, you can view the system transaction ledger on the main dashboard page.";
        } else if (lowerMessage.contains("exam") || lowerMessage.contains("test")) {
            return "To submit your exam form, please go to the 'Exams' page in the sidebar menu. Enter your candidate details and subject choices, then click 'Submit Form' to process your registration and matching dues.";
        } else if (lowerMessage.contains("result") || lowerMessage.contains("grade") || lowerMessage.contains("marks") || lowerMessage.contains("score")) {
            return "Students can view their registered course scores and evaluations in the 'Evaluation Roster' on the main dashboard. Faculty members can publish new marks and grades using the 'Submit Grades' tab.";
        } else if (lowerMessage.contains("attendance") || lowerMessage.contains("present")) {
            return "Faculty members can select their course to record attendance in the 'Mark Attendance' tab. Students can view their recorded class attendance logs inside their individual course details panel.";
        } else if (lowerMessage.contains("user") || lowerMessage.contains("register") || lowerMessage.contains("account") || lowerMessage.contains("profile")) {
            return "Administrators can register new student or faculty accounts and manage system roles using the 'User Management' panel on the sidebar.";
        } else if (lowerMessage.contains("setting") || lowerMessage.contains("config") || lowerMessage.contains("admin")) {
            return "As an administrator, you can update security settings, configure system-wide parameters, and review active sessions inside the 'Settings' panel on the sidebar.";
        } else if (lowerMessage.contains("hi") || lowerMessage.contains("hello") || lowerMessage.contains("hey") || lowerMessage.contains("help")) {
            if ("STUDENT".equalsIgnoreCase(userRole)) {
                return "Hello! As a student, you can check your enrollments, view academic scores in the 'Evaluation Roster', or click 'Pay Due' in the fees panel. How can I help you today?";
            } else if ("FACULTY".equalsIgnoreCase(userRole)) {
                return "Hello! As a faculty member, you can mark attendance, submit final grades, or manage assignment submissions. What task can I assist you with today?";
            } else {
                return "Hello! As an administrator, you can manage user accounts in 'User Management' and update portal configs in the 'Settings' section. How can I help you manage the ERP today?";
            }
        }

        // Default role-specific response if no keywords match
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            return "Hello! As a student, you can view your class schedule in the 'Current Course Enrollments' panel and check your scores in the 'Evaluation Roster'. You can also view your CGPA card in the header, or pay university dues by clicking the 'Pay Due' button in the fees section.";
        } else if ("FACULTY".equalsIgnoreCase(userRole)) {
            return "Welcome! As a faculty member, you can select your course to record attendance in the 'Mark Attendance' tab, and submit marks using the 'Submit Grades' form. You can also publish assignments and grade solutions on your panel.";
        } else {
            return "Hello! As an administrator, you can manage user accounts and register new members in the 'User Management' menu. You can also view financial summary cards on the main dashboard, or update security configurations in your settings panel.";
        }
    }
}
