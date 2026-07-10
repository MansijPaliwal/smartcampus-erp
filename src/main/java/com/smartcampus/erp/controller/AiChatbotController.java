package com.smartcampus.erp.controller;

import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.AiAdvisorService;
import com.smartcampus.erp.service.AiChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@Tag(name = "Support Chatbot", description = "AI operational helper and academic advisor endpoints")
public class AiChatbotController {

    private final AiChatbotService aiChatbotService;
    private final AiAdvisorService aiAdvisorService;

    public AiChatbotController(AiChatbotService aiChatbotService, AiAdvisorService aiAdvisorService) {
        this.aiChatbotService = aiChatbotService;
        this.aiAdvisorService = aiAdvisorService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Ask the AI assistant for application help", description = "Analyzes the authenticated user's role context to provide specific navigation assistance for application features.")
    public ResponseEntity<Map<String, String>> askAssistant(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> request) {
        
        String userMessage = request.getOrDefault("message", "");
        String roleStr = userPrincipal.getUser().getRole().name();
        
        try {
            String aiResponse = aiChatbotService.getFeatureHelpResponse(roleStr, userMessage);
            return ResponseEntity.ok(Map.of("response", aiResponse != null ? aiResponse : ""));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("response", "I am currently experiencing high traffic or network issues. Please try asking your question again later."));
        }
    }

    @PostMapping("/advisor/advise")
    @Operation(summary = "Ask the AI academic advisor for rulebook and syllabus guidance", description = "Executes similarity search on campus policy vector database to provide grounded advisor responses.")
    public ResponseEntity<Map<String, String>> askAdvisor(@RequestBody Map<String, String> request) {
        String userMessage = request.getOrDefault("message", "");
        try {
            String response = aiAdvisorService.advise(userMessage);
            return ResponseEntity.ok(Map.of("response", response != null ? response : ""));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("response", "I am currently experiencing high traffic or network issues. Please try asking your question again later."));
        }
    }
}
