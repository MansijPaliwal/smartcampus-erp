package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.GpaResponse;

public interface GpaService {
    GpaResponse calculateGpa(Long studentUserId);
}
