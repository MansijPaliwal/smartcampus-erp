package com.smartcampus.erp.billing.event;

import com.smartcampus.erp.academics.event.StudentEnrolledEvent;
import com.smartcampus.erp.dto.CreateFeeDueRequest;
import com.smartcampus.erp.service.FeePaymentService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class StudentEnrollmentBillingListener {

    private final FeePaymentService feePaymentService;

    public StudentEnrollmentBillingListener(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStudentEnrolled(StudentEnrolledEvent event) {
        CreateFeeDueRequest request = CreateFeeDueRequest.builder()
                .studentId(event.studentUserId())
                .amount(event.feeAmount())
                .build();
        feePaymentService.createFeeDue(request);
    }
}
