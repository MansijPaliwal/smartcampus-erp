package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByRollNumber(String rollNumber);
    boolean existsByRollNumber(String rollNumber);
}
