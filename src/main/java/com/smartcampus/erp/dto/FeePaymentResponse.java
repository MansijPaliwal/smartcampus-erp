package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;
    private String transactionId;
    private Long studentId;
    private String studentName;
    private String paymentMethod;
}
