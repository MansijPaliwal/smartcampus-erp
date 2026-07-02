package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeeStatusResponse {
    private BigDecimal totalDues;
    private BigDecimal totalPaid;
    private BigDecimal pendingDues;
    private List<FeePaymentResponse> paymentHistory;
}
