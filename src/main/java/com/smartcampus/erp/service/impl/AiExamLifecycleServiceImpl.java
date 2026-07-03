package com.smartcampus.erp.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartcampus.erp.entity.Course;
import com.smartcampus.erp.entity.ExamForm;
import com.smartcampus.erp.entity.Marks;
import com.smartcampus.erp.entity.StudentProfile;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.CourseRepository;
import com.smartcampus.erp.repository.ExamFormRepository;
import com.smartcampus.erp.repository.MarksRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.ExamLifecycleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Service
public class AiExamLifecycleServiceImpl implements ExamLifecycleService {

    private final ExamFormRepository examFormRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CourseRepository courseRepository;
    private final MarksRepository marksRepository;

    public AiExamLifecycleServiceImpl(
            ExamFormRepository examFormRepository,
            StudentProfileRepository studentProfileRepository,
            CourseRepository courseRepository,
            MarksRepository marksRepository) {
        this.examFormRepository = examFormRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.courseRepository = courseRepository;
        this.marksRepository = marksRepository;
    }

    @Override
    @Transactional
    public ExamForm submitExamFormAndPay(Long studentUserId, String examId, String candidateName, BigDecimal amount,
            String subjectDetails) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentUserId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        // Create transaction details
        String txId = "TX-EXAM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String examDate = LocalDate.now().plusDays(15).toString();
        String center = "AKTU State Evaluation Center, Noida Campus, Sector 62, Noida, UP";

        ExamForm form = ExamForm.builder()
                .examId(examId)
                .candidateName(candidateName)
                .paymentStatus("PAID")
                .paymentTransactionId(txId)
                .allocatedExamDate(examDate)
                .examCenter(center)
                .studentProfile(studentProfile)
                .subjectDetails(subjectDetails)
                .build();

        return examFormRepository.save(form);
    }

    @Override
    public byte[] generateAdmitCardPdf(Long studentUserId) {
        ExamForm form = examFormRepository
                .findFirstByStudentProfileIdAndPaymentStatusOrderByIdDesc(studentUserId, "PAID")
                .orElseThrow(
                        () -> new ResourceNotFoundException("No paid exam form registrations found for this student."));

        StudentProfile profile = form.getStudentProfile();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter.getInstance(document, out);

            document.open();

            // Font configurations
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Title
            Paragraph title = new Paragraph("DR. A.P.J. ABDUL KALAM TECHNICAL UNIVERSITY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph subtitle = new Paragraph("OFFICIAL EXAMINATION ADMIT CARD", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Candidate Information Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            infoTable.addCell(createCell("Candidate Name:", boldFont));
            infoTable.addCell(createCell(form.getCandidateName(), normalFont));

            infoTable.addCell(createCell("Roll Number:", boldFont));
            infoTable.addCell(createCell(profile.getRollNumber(), normalFont));

            infoTable.addCell(createCell("Department / Semester:", boldFont));
            infoTable.addCell(createCell(profile.getDepartment() + " / Semester " + profile.getSemester(), normalFont));

            infoTable.addCell(createCell("Exam Center / Location:", boldFont));
            infoTable.addCell(createCell(form.getExamCenter(), normalFont));

            infoTable.addCell(createCell("Allocated Exam Date:", boldFont));
            infoTable.addCell(createCell(form.getAllocatedExamDate(), normalFont));

            infoTable.addCell(createCell("Payment Transaction Reference:", boldFont));
            infoTable.addCell(createCell(form.getPaymentTransactionId(), normalFont));

            document.add(infoTable);

            // AKTU Course Schedule Table
            Paragraph scheduleTitle = new Paragraph("Syllabus Course Schedule & Evaluation Roster", subtitleFont);
            scheduleTitle.setSpacingAfter(10);
            document.add(scheduleTitle);

            PdfPTable scheduleTable = new PdfPTable(4);
            scheduleTable.setWidthPercentage(100);
            scheduleTable.addCell(createCell("Course Code", boldFont));
            scheduleTable.addCell(createCell("Course Name", boldFont));
            scheduleTable.addCell(createCell("Exam Center Session", boldFont));
            scheduleTable.addCell(createCell("Timing", boldFont));

            // Load courses from the saved subjectDetails instead of querying all department
            // courses
            String subjectDetails = form.getSubjectDetails();
            if (subjectDetails != null && !subjectDetails.trim().isEmpty()) {
                String[] subjects = subjectDetails.split("\\|");
                for (String subj : subjects) {
                    String[] parts = subj.split(":", 2);
                    String code = parts[0].trim();
                    String name = parts.length > 1 ? parts[1].trim() : "";

                    scheduleTable.addCell(createCell(code, normalFont));
                    scheduleTable.addCell(createCell(name, normalFont));
                    scheduleTable.addCell(createCell("Main Exam Block", normalFont));
                    scheduleTable.addCell(createCell("09:30 AM - 12:30 PM", normalFont));
                }
            } else {
                List<Course> courses = courseRepository.findByDepartment(profile.getDepartment());
                for (Course course : courses) {
                    if (course.getCode().contains(String.valueOf(profile.getSemester()))) {
                        scheduleTable.addCell(createCell(course.getCode(), normalFont));
                        scheduleTable.addCell(createCell(course.getTitle(), normalFont));
                        scheduleTable.addCell(createCell("Main Exam Block", normalFont));
                        scheduleTable.addCell(createCell("09:30 AM - 12:30 PM", normalFont));
                    }
                }
            }

            document.add(scheduleTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during programmatic admit card PDF stream creation", e);
        }
    }

    @Override
    public Map<String, Object> getAcademicResults(Long studentUserId) {
        StudentProfile profile = studentProfileRepository.findById(studentUserId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        // Verify that a paid exam form exists for this student
        examFormRepository.findFirstByStudentProfileIdAndPaymentStatusOrderByIdDesc(studentUserId, "PAID")
                .orElseThrow(
                        () -> new ResourceNotFoundException("No paid exam form registrations found for this student."));

        List<Marks> marksList = marksRepository.findByStudentId(studentUserId);

        List<Map<String, Object>> subjectGrades = new ArrayList<>();
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Marks marks : marksList) {
            double percentage = (marks.getMarksObtained().doubleValue() / marks.getMaxMarks().doubleValue()) * 100;
            String letterGrade = getLetterGrade(percentage);
            int gradePoints = getGradePoints(letterGrade);
            int credits = marks.getCourse().getCredits();

            BigDecimal coursePoints = BigDecimal.valueOf(gradePoints * credits);
            totalPoints = totalPoints.add(coursePoints);
            totalCredits = totalCredits.add(BigDecimal.valueOf(credits));

            Map<String, Object> subjectMap = new HashMap<>();
            subjectMap.put("courseCode", marks.getCourse().getCode());
            subjectMap.put("courseTitle", marks.getCourse().getTitle());
            subjectMap.put("examType", marks.getExamType());
            subjectMap.put("percentage", BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP));
            subjectMap.put("grade", letterGrade);
            subjectMap.put("credits", credits);

            subjectGrades.add(subjectMap);
        }

        BigDecimal aggregateCpa = BigDecimal.ZERO;
        if (totalCredits.compareTo(BigDecimal.ZERO) > 0) {
            aggregateCpa = totalPoints.divide(totalCredits, 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> results = new HashMap<>();
        results.put("rollNumber", profile.getRollNumber());
        results.put("candidateName", profile.getUser().getName());
        results.put("semester", profile.getSemester());
        results.put("subjects", subjectGrades);
        results.put("cgpa", aggregateCpa);

        Optional<ExamForm> examFormOpt = examFormRepository
                .findFirstByStudentProfileIdAndPaymentStatusOrderByIdDesc(studentUserId, "PAID");
        if (examFormOpt.isPresent()) {
            results.put("registeredSubjects", examFormOpt.get().getSubjectDetails());
        }

        return results;
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }

    private String getLetterGrade(double percentage) {
        if (percentage >= 90.0)
            return "A+"; // Outstanding
        if (percentage >= 80.0)
            return "A"; // Excellent
        if (percentage >= 70.0)
            return "B+";
        if (percentage >= 60.0)
            return "B";
        if (percentage >= 50.0)
            return "C";
        return "C";
    }

    private int getGradePoints(String grade) {
        switch (grade) {
            case "A+":
                return 10;
            case "A":
                return 9;
            case "B+":
                return 8;
            case "B":
                return 7;
            case "C":
                return 6;
            default:
                return 5;
        }
    }
}
