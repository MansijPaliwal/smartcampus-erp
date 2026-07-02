package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.GpaResponse;
import com.smartcampus.erp.entity.Course;
import com.smartcampus.erp.entity.Marks;
import com.smartcampus.erp.repository.MarksRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.impl.GpaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GpaServiceTest {

    @Mock
    private MarksRepository marksRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private GpaServiceImpl gpaService;

    @Test
    void calculateGpa_success() {
        Long studentId = 1L;
        when(studentProfileRepository.existsById(studentId)).thenReturn(true);

        Course math = Course.builder().id(101L).code("MATH101").title("Math").credits(4).build();
        Course physics = Course.builder().id(102L).code("PHY101").title("Physics").credits(3).build();

        Marks mathMid = Marks.builder().course(math).examType("MIDTERM").marksObtained(java.math.BigDecimal.valueOf(40.0)).maxMarks(java.math.BigDecimal.valueOf(50.0)).build();
        Marks mathFin = Marks.builder().course(math).examType("FINAL").marksObtained(java.math.BigDecimal.valueOf(45.0)).maxMarks(java.math.BigDecimal.valueOf(50.0)).build();

        Marks phyQuiz = Marks.builder().course(physics).examType("QUIZ").marksObtained(java.math.BigDecimal.valueOf(8.0)).maxMarks(java.math.BigDecimal.valueOf(10.0)).build();
        Marks phyFin = Marks.builder().course(physics).examType("FINAL").marksObtained(java.math.BigDecimal.valueOf(72.0)).maxMarks(java.math.BigDecimal.valueOf(90.0)).build();

        List<Marks> marks = Arrays.asList(mathMid, mathFin, phyQuiz, phyFin);
        when(marksRepository.findByStudentId(studentId)).thenReturn(marks);

        GpaResponse response = gpaService.calculateGpa(studentId);

        assertNotNull(response);
        assertEquals(9.0, response.getGpa());
        assertEquals(2, response.getCourseGrades().size());
    }
}
