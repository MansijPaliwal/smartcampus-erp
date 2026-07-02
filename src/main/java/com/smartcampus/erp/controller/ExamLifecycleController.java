package com.smartcampus.erp.controller;

import com.smartcampus.erp.entity.ExamForm;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.ExamLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@Tag(name = "Exam Lifecycle Manager", description = "Endpoints for student exam form submissions, payment matching, dynamic PDF admit cards, and AKTU markbook results")
public class ExamLifecycleController {

    private final ExamLifecycleService examLifecycleService;

    public ExamLifecycleController(ExamLifecycleService examLifecycleService) {
        this.examLifecycleService = examLifecycleService;
    }

    @PostMapping("/form/submit")
    @Operation(summary = "Submit exam registration form and execute billing ledger check")
    public ResponseEntity<ExamForm> submitForm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> payload) {
        
        String examId = payload.getOrDefault("examId", "EXAM-MAIN-2026");
        String candidateName = payload.getOrDefault("candidateName", userPrincipal.getUser().getName());

        ExamForm form = examLifecycleService.submitExamFormAndPay(
                userPrincipal.getId(),
                examId,
                candidateName,
                BigDecimal.valueOf(1500.00)
        );
        return ResponseEntity.ok(form);
    }

    @GetMapping("/admit-card/download")
    @Operation(summary = "Download official AKTU PDF admit card document stream")
    public ResponseEntity<byte[]> downloadAdmitCard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        byte[] pdfBytes = examLifecycleService.generateAdmitCardPdf(userPrincipal.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aktu-admit-card-" + userPrincipal.getId() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/results")
    @Operation(summary = "Fetch student recorded course scores and calculated semester CGPA")
    public ResponseEntity<Map<String, Object>> getResults(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> results = examLifecycleService.getAcademicResults(userPrincipal.getId());
        return ResponseEntity.ok(results);
    }
}
