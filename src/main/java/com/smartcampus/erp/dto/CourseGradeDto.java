package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseGradeDto {
    private String courseCode;
    private String courseTitle;
    private Double percentage;
    private String letterGrade;
    private Integer gradePoint;
    private Integer credits;
}
