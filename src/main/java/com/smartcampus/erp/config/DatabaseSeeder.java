package com.smartcampus.erp.config;

import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          FacultyProfileRepository facultyProfileRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          AttendanceRepository attendanceRepository,
                          MarksRepository marksRepository,
                          FeePaymentRepository feePaymentRepository,
                          NotificationRepository notificationRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() <= 1) {
            attendanceRepository.deleteAll();
            marksRepository.deleteAll();
            enrollmentRepository.deleteAll();
            feePaymentRepository.deleteAll();
            notificationRepository.deleteAll();
            courseRepository.deleteAll();
            studentProfileRepository.deleteAll();
            facultyProfileRepository.deleteAll();
            userRepository.deleteAll();

            // 1. Create and Save Admin User (no associated profile)
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin.system@smartcampus.edu")
                    .password(passwordEncoder.encode("AdminSystem@2026"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);

            // 2. Create Faculty Profiles (cascades User creation)
            User facultyUser1 = User.builder()
                    .name("Dr. Alan Turing")
                    .email("alan.turing@smartcampus.edu")
                    .password(passwordEncoder.encode("AlanFaculty@2026"))
                    .role(Role.FACULTY)
                    .enabled(true)
                    .build();
            FacultyProfile facultyProfile1 = FacultyProfile.builder()
                    .user(facultyUser1)
                    .department("Computer Science")
                    .designation("Professor / Chair")
                    .build();
            facultyProfile1 = facultyProfileRepository.save(facultyProfile1);

            User facultyUser2 = User.builder()
                    .name("Dr. Ada Lovelace")
                    .email("lovelace@smartcampus.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.FACULTY)
                    .enabled(true)
                    .build();
            FacultyProfile facultyProfile2 = FacultyProfile.builder()
                    .user(facultyUser2)
                    .department("Mathematics")
                    .designation("Senior Lecturer")
                    .build();
            facultyProfile2 = facultyProfileRepository.save(facultyProfile2);

            // 3. Create Student Profiles (cascades User creation)
            User studentUser1 = User.builder()
                    .name("Aarav Sharma")
                    .email("aarav.sharma@smartcampus.edu")
                    .password(passwordEncoder.encode("AaravStudent@2026"))
                    .role(Role.STUDENT)
                    .enabled(true)
                    .build();
            StudentProfile studentProfile1 = StudentProfile.builder()
                    .user(studentUser1)
                    .rollNumber("CS-2026-0045")
                    .department("Computer Science")
                    .semester(3)
                    .dob(LocalDate.of(2004, 10, 15))
                    .phone("+91-9876543210")
                    .build();
            studentProfile1 = studentProfileRepository.save(studentProfile1);

            User studentUser2 = User.builder()
                    .name("Priya Patel")
                    .email("bob@smartcampus.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.STUDENT)
                    .enabled(true)
                    .build();
            StudentProfile studentProfile2 = StudentProfile.builder()
                    .user(studentUser2)
                    .rollNumber("CS-2026-0046")
                    .department("Computer Science")
                    .semester(3)
                    .dob(LocalDate.of(2004, 5, 20))
                    .phone("+91-9876543211")
                    .build();
            studentProfile2 = studentProfileRepository.save(studentProfile2);

            User studentUser3 = User.builder()
                    .name("Rohan Gupta")
                    .email("charlie@smartcampus.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.STUDENT)
                    .enabled(true)
                    .build();
            StudentProfile studentProfile3 = StudentProfile.builder()
                    .user(studentUser3)
                    .rollNumber("MA-2026-0012")
                    .department("Mathematics")
                    .semester(1)
                    .dob(LocalDate.of(2005, 8, 12))
                    .phone("+91-9876543212")
                    .build();
            studentProfile3 = studentProfileRepository.save(studentProfile3);

            // 4. Create Courses
            Course course1 = Course.builder()
                    .code("CS101")
                    .title("Introduction to Java Programming")
                    .credits(4)
                    .department("Computer Science")
                    .faculty(facultyProfile1)
                    .build();
            courseRepository.save(course1);

            Course course2 = Course.builder()
                    .code("CS201")
                    .title("Data Structures & Algorithms")
                    .credits(4)
                    .department("Computer Science")
                    .faculty(facultyProfile1)
                    .build();
            courseRepository.save(course2);

            Course course3 = Course.builder()
                    .code("MA101")
                    .title("Single Variable Calculus")
                    .credits(3)
                    .department("Mathematics")
                    .faculty(facultyProfile2)
                    .build();
            courseRepository.save(course3);

            // AKTU Semester 3 CSE Courses
            // Theory Courses (6)
            Course kcs301 = Course.builder()
                    .code("KCS301").title("Data Structures").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs301);

            Course kcs302 = Course.builder()
                    .code("KCS302").title("Computer Organization and Architecture").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs302);

            Course kcs303 = Course.builder()
                    .code("KCS303").title("Discrete Structures & Theory of Logic").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs303);

            Course kas301 = Course.builder()
                    .code("KAS301").title("Technical Communication").credits(2)
                    .department("Computer Science").faculty(facultyProfile2).build();
            courseRepository.save(kas301);

            Course koe031 = Course.builder()
                    .code("KOE031").title("Cyber Security").credits(3)
                    .department("Computer Science").faculty(facultyProfile2).build();
            courseRepository.save(koe031);

            Course kve301 = Course.builder()
                    .code("KVE301").title("Universal Human Values").credits(3)
                    .department("Computer Science").faculty(facultyProfile2).build();
            courseRepository.save(kve301);

            // Lab Courses (4)
            Course kcs351 = Course.builder()
                    .code("KCS351").title("Data Structures Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs351);

            Course kcs352 = Course.builder()
                    .code("KCS352").title("Computer Organization Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs352);

            Course kcs353 = Course.builder()
                    .code("KCS353").title("Discrete Structure Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs353);

            Course kcs354 = Course.builder()
                    .code("KCS354").title("Mini Project Assessment").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs354);


            // AKTU Semester 5 CSE Courses
            // Theory Courses (6)
            Course kcs501 = Course.builder()
                    .code("KCS501").title("Database Management System").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs501);

            Course kcs502 = Course.builder()
                    .code("KCS502").title("Compiler Design").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs502);

            Course kcs503 = Course.builder()
                    .code("KCS503").title("Design and Analysis of Algorithm").credits(4)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs503);

            Course kcs051 = Course.builder()
                    .code("KCS051").title("Web Technology").credits(3)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs051);

            Course kcs054 = Course.builder()
                    .code("KCS054").title("Object Oriented System Design").credits(3)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs054);

            Course knc501 = Course.builder()
                    .code("KNC501").title("Constitution of India").credits(2)
                    .department("Computer Science").faculty(facultyProfile2).build();
            courseRepository.save(knc501);

            // Lab Courses (4)
            Course kcs551 = Course.builder()
                    .code("KCS551").title("DBMS Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs551);

            Course kcs552 = Course.builder()
                    .code("KCS552").title("Compiler Design Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs552);

            Course kcs553 = Course.builder()
                    .code("KCS553").title("DAA Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs553);

            Course kcs554 = Course.builder()
                    .code("KCS554").title("Web Technology Lab").credits(1)
                    .department("Computer Science").faculty(facultyProfile1).build();
            courseRepository.save(kcs554);

            // 5. Create Enrollments
            Enrollment enrollment1 = Enrollment.builder()
                    .student(studentProfile1)
                    .course(course1)
                    .enrollmentDate(LocalDate.now().minusWeeks(4))
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment1);

            Enrollment enrollment2 = Enrollment.builder()
                    .student(studentProfile1)
                    .course(course2)
                    .enrollmentDate(LocalDate.now().minusWeeks(4))
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment2);

            Enrollment enrollment3 = Enrollment.builder()
                    .student(studentProfile2)
                    .course(course1)
                    .enrollmentDate(LocalDate.now().minusWeeks(3))
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment3);

            Enrollment enrollment4 = Enrollment.builder()
                    .student(studentProfile3)
                    .course(course3)
                    .enrollmentDate(LocalDate.now().minusWeeks(2))
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment4);

            // 6. Seed Marks
            // Theory Marks (6)
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs301).examType("FINAL").marksObtained(BigDecimal.valueOf(85.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs302).examType("FINAL").marksObtained(BigDecimal.valueOf(78.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs303).examType("FINAL").marksObtained(BigDecimal.valueOf(92.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kas301).examType("FINAL").marksObtained(BigDecimal.valueOf(88.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(koe031).examType("FINAL").marksObtained(BigDecimal.valueOf(74.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kve301).examType("FINAL").marksObtained(BigDecimal.valueOf(95.0)).maxMarks(BigDecimal.valueOf(100.0)).build());
            
            // Lab Marks (4)
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs351).examType("FINAL_LAB").marksObtained(BigDecimal.valueOf(48.0)).maxMarks(BigDecimal.valueOf(50.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs352).examType("FINAL_LAB").marksObtained(BigDecimal.valueOf(45.0)).maxMarks(BigDecimal.valueOf(50.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs353).examType("FINAL_LAB").marksObtained(BigDecimal.valueOf(42.0)).maxMarks(BigDecimal.valueOf(50.0)).build());
            marksRepository.save(Marks.builder().student(studentProfile1).course(kcs354).examType("FINAL_LAB").marksObtained(BigDecimal.valueOf(47.0)).maxMarks(BigDecimal.valueOf(50.0)).build());

            // 7. Seed Attendance
            attendanceRepository.save(Attendance.builder()
                    .student(studentProfile1)
                    .course(course1)
                    .date(LocalDate.now().minusDays(2))
                    .status(AttendanceStatus.PRESENT)
                    .build());

            attendanceRepository.save(Attendance.builder()
                    .student(studentProfile1)
                    .course(course1)
                    .date(LocalDate.now().minusDays(1))
                    .status(AttendanceStatus.PRESENT)
                    .build());

            attendanceRepository.save(Attendance.builder()
                    .student(studentProfile2)
                    .course(course1)
                    .date(LocalDate.now().minusDays(1))
                    .status(AttendanceStatus.ABSENT)
                    .build());

            // 8. Seed Tuition Billing Invoices & Payments
            feePaymentRepository.save(FeePayment.builder()
                    .student(studentProfile1)
                    .amount(BigDecimal.valueOf(1250.0))
                    .status(PaymentStatus.PAID)
                    .paymentDate(LocalDateTime.now().minusDays(5))
                    .transactionId(UUID.randomUUID().toString())
                    .paymentMethod("UPI")
                    .build());

            feePaymentRepository.save(FeePayment.builder()
                    .student(studentProfile1)
                    .amount(BigDecimal.valueOf(1500.0))
                    .status(PaymentStatus.PENDING)
                    .build());

            feePaymentRepository.save(FeePayment.builder()
                    .student(studentProfile1)
                    .amount(BigDecimal.valueOf(3200.0))
                    .status(PaymentStatus.PENDING)
                    .build());

            feePaymentRepository.save(FeePayment.builder()
                    .student(studentProfile2)
                    .amount(BigDecimal.valueOf(1250.0))
                    .status(PaymentStatus.PENDING)
                    .build());

            // 9. Seed Notifications Alerts
            notificationRepository.save(Notification.builder()
                    .user(studentProfile1.getUser())
                    .title("Welcome to SmartCampus!")
                    .message("Your student profile has been successfully set up for the CS department.")
                    .createdAt(LocalDateTime.now().minusWeeks(1))
                    .isRead(true)
                    .build());

            notificationRepository.save(Notification.builder()
                    .user(studentProfile1.getUser())
                    .title("Pending Semester Fee Payment")
                    .message("Please pay the pending tuition bill of $1500.00 before the due date.")
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .isRead(false)
                    .build());

            System.out.println("Seeded database with rich mock statistics dataset successfully.");
        }
    }
}
