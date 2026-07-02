package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {
}
