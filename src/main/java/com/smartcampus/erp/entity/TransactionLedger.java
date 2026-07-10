package com.smartcampus.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal debit;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal credit;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "previous_hash", nullable = false)
    private String previousHash;

    @Column(name = "current_hash", nullable = false)
    private String currentHash;
}
