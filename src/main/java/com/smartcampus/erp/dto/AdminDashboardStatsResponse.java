package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsResponse {
    private Long studentCount;
    private Long facultyCount;
    private Long courseCount;
    private Double totalPendingFees;
}
