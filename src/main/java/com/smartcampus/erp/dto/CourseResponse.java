package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String code;
    private String title;
    private Integer credits;
    private String department;
    private String facultyName;
    private Long facultyId;
}
