package com.smartcampus.erp.controller;

import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.AiChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@Tag(name = "Support Chatbot", description = "AI operational helper endpoint for application assistance and feature navigation guidelines")
public class AiChatbotController {

    private final AiChatbotService aiChatbotService;

    public AiChatbotController(AiChatbotService aiChatbotService) {
        this.aiChatbotService = aiChatbotService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Ask the AI assistant for application help", description = "Analyzes the authenticated user's role context to provide specific navigation assistance for application features.")
    public ResponseEntity<Map<String, String>> askAssistant(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> request) {
        
        String userMessage = request.getOrDefault("message", "");
        String roleStr = userPrincipal.getUser().getRole().name();
        
        String aiResponse = aiChatbotService.getFeatureHelpResponse(roleStr, userMessage);
        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}
