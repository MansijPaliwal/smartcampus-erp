-- Baseline Schema Initialization for SmartCampus ERP
-- Suitable for both MySQL and H2 (MySQL Mode)

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role ENUM('ADMIN', 'FACULTY', 'STUDENT') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email)
);

CREATE TABLE student_profiles (
    user_id BIGINT NOT NULL,
    address VARCHAR(1000),
    department VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(255),
    phone VARCHAR(255) NOT NULL,
    roll_number VARCHAR(255) NOT NULL,
    semester INTEGER NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT UK23wdqfc85p2k2fjkofadnl4m1 UNIQUE (roll_number),
    CONSTRAINT FK32koy3tgqtaujxhfsn0b9pel2 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE faculty_profiles (
    user_id BIGINT NOT NULL,
    department VARCHAR(255) NOT NULL,
    designation VARCHAR(255) NOT NULL,
    joining_date DATE,
    phone VARCHAR(255),
    specialization VARCHAR(255),
    PRIMARY KEY (user_id),
    CONSTRAINT FKaj66kj78fcavpppj54g0e6aij FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    credits INTEGER NOT NULL,
    department VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    faculty_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT UK61og8rbqdd2y28rx2et5fdnxd UNIQUE (code),
    CONSTRAINT FKepbsdwwdn9gu6gdphkhboqt97 FOREIGN KEY (faculty_id) REFERENCES faculty_profiles (user_id)
);

CREATE TABLE enrollments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    enrollment_date DATE NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'DROPPED', 'PENDING') NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKi0g6mfijtuh199nj653nva6j5 UNIQUE (student_id, course_id),
    CONSTRAINT FKho8mcicp4196ebpltdn9wl6co FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT FKenh264ay3d1nd1lncabcwu59g FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);

CREATE TABLE assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description TEXT,
    due_date TIMESTAMP(6) NOT NULL,
    max_marks DECIMAL(38,2) NOT NULL,
    title VARCHAR(255) NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK6p1m72jobsvmrrn4bpj4168mg FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE assignment_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_url VARCHAR(255) NOT NULL,
    marks_obtained DECIMAL(38,2),
    submitted_at TIMESTAMP(6) NOT NULL,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKjpaoiqlq2bm3rv52lcri47g4s UNIQUE (assignment_id, student_id),
    CONSTRAINT FKm7i7ubgh7y2n6mvg8muw62oax FOREIGN KEY (assignment_id) REFERENCES assignments (id),
    CONSTRAINT FK5qnq3hp86knsxdfh46te5o1x1 FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);

CREATE TABLE attendance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    date DATE NOT NULL,
    status ENUM('ABSENT', 'PRESENT') NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UK3mjvpj1ab0m02i68nvmeduamb UNIQUE (student_id, course_id, date),
    CONSTRAINT FKn38ldxe7u4udeu15ikqfsplnm FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT FKometqpvkmvfwrw8w8b13s5gkq FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);

CREATE TABLE fee_payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(38,2) NOT NULL,
    payment_date TIMESTAMP(6),
    payment_method VARCHAR(255),
    status ENUM('FAILED', 'PAID', 'PENDING') NOT NULL,
    transaction_id VARCHAR(255),
    student_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKi799xb6yuy6aufqov5aye5hd5 UNIQUE (transaction_id),
    CONSTRAINT FKlrmq7ul2my7y02eb6hsqmwois FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);

CREATE TABLE marks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_type VARCHAR(255) NOT NULL,
    marks_obtained DECIMAL(38,2) NOT NULL,
    max_marks DECIMAL(38,2) NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKc4cir2e6y6unesqdhbj9te17t UNIQUE (student_id, course_id, exam_type),
    CONSTRAINT FKqmibkydr77dcr0r32lo91regw FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT FKdurlvmr13p0t42h03ho106s7f FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP(6) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    message TEXT NOT NULL,
    title VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE exam_forms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    allocated_exam_date VARCHAR(255),
    candidate_name VARCHAR(255) NOT NULL,
    exam_center VARCHAR(255),
    exam_id VARCHAR(255) NOT NULL,
    payment_status VARCHAR(255) NOT NULL,
    payment_transaction_id VARCHAR(255),
    subject_details VARCHAR(2000),
    student_profile_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK3mhgire784ushwf3mk96dl97w FOREIGN KEY (student_profile_id) REFERENCES student_profiles (user_id)
);
