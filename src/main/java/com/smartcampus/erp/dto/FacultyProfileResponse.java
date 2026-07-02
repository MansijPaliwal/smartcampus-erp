package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String designation;
    private String phone;
    private LocalDate joiningDate;
    private String specialization;
}
