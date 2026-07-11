package com.smartcampus.erp.academics.event;

import java.math.BigDecimal;

public record StudentEnrolledEvent(Long studentUserId, BigDecimal feeAmount) {
}
