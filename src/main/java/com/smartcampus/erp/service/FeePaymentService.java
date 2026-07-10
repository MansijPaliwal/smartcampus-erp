package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.CreateFeeDueRequest;
import com.smartcampus.erp.dto.FeePaymentRequest;
import com.smartcampus.erp.dto.FeePaymentResponse;

import java.util.List;
import java.util.Map;

public interface FeePaymentService {
    FeePaymentResponse createFeeDue(CreateFeeDueRequest request);
    FeePaymentResponse payFee(Long studentUserId, Long feePaymentId, FeePaymentRequest request);
    List<FeePaymentResponse> getAllPayments();
    void processPaymentWebhook(Map<String, Object> payload);
}
