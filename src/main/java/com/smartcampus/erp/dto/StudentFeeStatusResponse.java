package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeeStatusResponse {
    private Double totalDues;
    private Double totalPaid;
    private Double pendingDues;
    private List<FeePaymentResponse> paymentHistory;
}
