package com.smartcampus.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.service.FeePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Payment Webhook Handler", description = "Secure payment processing webhooks verifying HMAC SHA-256 signatures")
public class WebhookController {

    private final FeePaymentService feePaymentService;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookController(FeePaymentService feePaymentService,
                             ObjectMapper objectMapper,
                             @Value("${app.webhook.secret:secure_webhook_secret_key_2026}") String webhookSecret) {
        this.feePaymentService = feePaymentService;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/payments")
    @Operation(summary = "Receive and verify Razorpay/Stripe payment events", description = "Verifies HMAC SHA-256 signatures of webhook events before processing status transitions.")
    public ResponseEntity<Map<String, String>> handlePaymentWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signatureHeader) {

        if (signatureHeader == null || signatureHeader.trim().isEmpty()) {
            throw new BadRequestException("Missing X-Webhook-Signature header security verification parameters");
        }

        // Verify the cryptographic HMAC SHA-256 signature of the raw body payload
        if (!verifyHmacSignature(rawPayload, signatureHeader, webhookSecret)) {
            throw new BadRequestException("Invalid webhook signature; validation failed");
        }

        try {
            // Parse payload string into Map representation
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = objectMapper.readValue(rawPayload, Map.class);
            feePaymentService.processPaymentWebhook(payloadMap);
            return ResponseEntity.ok(Map.of("status", "processed", "message", "Webhook verified and processed"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Failed to parse and process payment webhook payload: " + e.getMessage());
        }
    }

    private boolean verifyHmacSignature(String data, String expectedSignature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Format signature to hex string
            String computedSignature = HexFormat.of().formatHex(signedBytes);
            
            // Constant-time comparison to prevent timing attacks
            return MessageDigestEquals(computedSignature.getBytes(StandardCharsets.UTF_8), 
                                        expectedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean MessageDigestEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
