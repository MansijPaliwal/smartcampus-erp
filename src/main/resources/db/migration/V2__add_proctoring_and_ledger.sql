-- Migration V2: Add Proctoring columns and Transaction Ledger table
-- Compatible with H2, MySQL, and PostgreSQL

ALTER TABLE assignment_submissions ADD COLUMN integrity_score INT DEFAULT NULL;
ALTER TABLE assignment_submissions ADD COLUMN ip_address VARCHAR(255) DEFAULT NULL;
ALTER TABLE assignment_submissions ADD COLUMN completion_time_seconds INT DEFAULT NULL;

CREATE TABLE transaction_ledger (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    debit DECIMAL(38,2) NOT NULL,
    credit DECIMAL(38,2) NOT NULL,
    balance DECIMAL(38,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    previous_hash VARCHAR(255) NOT NULL,
    current_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_ledger_student FOREIGN KEY (student_id) REFERENCES student_profiles (user_id)
);
