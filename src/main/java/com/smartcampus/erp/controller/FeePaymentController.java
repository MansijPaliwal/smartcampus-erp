package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.CreateFeeDueRequest;
import com.smartcampus.erp.dto.FeePaymentRequest;
import com.smartcampus.erp.dto.FeePaymentResponse;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.FeePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@Tag(name = "Fees", description = "Fee management, dues generation for admins, and online mock payments for students")
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    @PostMapping("/due")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate fee due", description = "Create a new pending fee invoice for a specific student. Accessible by Admin.")
    @ApiResponse(responseCode = "201", description = "Successfully generated fee invoice")
    @ApiResponse(responseCode = "404", description = "Student profile not found")
    public ResponseEntity<FeePaymentResponse> createFeeDue(@Valid @RequestBody CreateFeeDueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feePaymentService.createFeeDue(request));
    }

    @PostMapping("/{feePaymentId}/pay")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Pay pending fee due", description = "Record a mock payment transaction for a pending fee due invoice using credit card details.")
    @ApiResponse(responseCode = "200", description = "Successfully completed payment transaction")
    @ApiResponse(responseCode = "400", description = "Fee already paid, or card validation fails")
    @ApiResponse(responseCode = "404", description = "Invoice not found or does not belong to logged-in student")
    public ResponseEntity<FeePaymentResponse> payFee(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long feePaymentId,
            @Valid @RequestBody FeePaymentRequest request) {
        return ResponseEntity.ok(feePaymentService.payFee(userPrincipal.getId(), feePaymentId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments history", description = "Retrieve list of all billing invoices and payments. Accessible by Admin.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments")
    public ResponseEntity<List<FeePaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(feePaymentService.getAllPayments());
    }
}
