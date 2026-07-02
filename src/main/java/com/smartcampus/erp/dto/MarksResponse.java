package com.smartcampus.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarksResponse {
    private Long id;
    private String courseCode;
    private String courseTitle;
    private String examType;
    private Double marksObtained;
    private Double maxMarks;
}
