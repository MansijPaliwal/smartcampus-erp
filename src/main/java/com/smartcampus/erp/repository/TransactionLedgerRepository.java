package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.TransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, Long> {
    
    /**
     * Retrieves the entire ledger history for a student ordered chronologically.
     */
    List<TransactionLedger> findByStudentIdOrderByIdAsc(Long studentId);

    /**
     * Finds the absolute latest ledger entry for a student, used to determine
     * the running balance and get the latest cryptographic hash.
     */
    Optional<TransactionLedger> findFirstByStudentIdOrderByIdDesc(Long studentId);
}
