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
public class StudentProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String rollNumber;
    private String department;
    private Integer semester;
    private LocalDate dob;
    private String phone;
    private String gender;
    private String address;
}
