package com.smartcampus.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttendanceDto {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required")
    private String status; // PRESENT, ABSENT
}
