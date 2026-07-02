package com.smartcampus.erp.service.impl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.smartcampus.erp.service.AiChatbotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiChatbotServiceImpl implements AiChatbotService {

    private final OpenAIClient openAiClient;

    public AiChatbotServiceImpl(@Value("${OPENAI_API_KEY:mock-key}") String apiKey) {
        this.openAiClient = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
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
            ChatCompletion completion = openAiClient.chat().completions().create(
                    ChatCompletionCreateParams.builder()
                            .model("gpt-4o-mini")
                            .addSystemMessage(systemPlaybook)
                            .addUserMessage(userMessage)
                            .maxTokens(200L)
                            .temperature(0.5)
                            .build()
            );

            return completion.choices().get(0).message().content().orElse("Please refer to the Swagger interactive API endpoints at /swagger-ui.html for schema rules.");
        } catch (Exception e) {
            e.printStackTrace();
            if ("STUDENT".equalsIgnoreCase(userRole)) {
                return "Hello! As a student, you can view your class schedule in the 'Current Course Enrollments' panel and check your scores in the 'Evaluation Roster'. You can also view your CGPA card in the header, or pay university dues by clicking the 'Pay Due' button in the fees section. Let me know if you need help with anything else!";
            } else if ("FACULTY".equalsIgnoreCase(userRole)) {
                return "Welcome! As a faculty member, you can select your course to record attendance in the 'Mark Attendance' tab, and submit marks using the 'Submit Grades' form. You can also publish assignments and grade solutions on your panel. Please let me know how else I can assist you!";
            } else {
                return "Hello! As an administrator, you can manage user accounts and register new members in the 'User Management' menu. You can also view financial summary cards on the main dashboard, or update security configurations in your settings panel. What task can I assist you with today?";
            }
        }
    }
}
