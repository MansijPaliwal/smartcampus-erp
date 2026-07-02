package com.smartcampus.erp.service;

import com.smartcampus.erp.entity.ExamForm;
import java.math.BigDecimal;
import java.util.Map;

public interface ExamLifecycleService {
    ExamForm submitExamFormAndPay(Long studentUserId, String examId, String candidateName, BigDecimal amount);
    byte[] generateAdmitCardPdf(Long studentUserId);
    Map<String, Object> getAcademicResults(Long studentUserId);
}
