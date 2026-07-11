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

    @PrePersist
    protected void calculateHash() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        String amountStr = (this.debit != null ? this.debit.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00")
                + "|" + (this.credit != null ? this.credit.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00")
                + "|" + (this.balance != null ? this.balance.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00");
                
        String timestampStr = this.createdAt.toString();
        String prevHashStr = this.previousHash != null ? this.previousHash : "0000000000000000000000000000000000000000000000000000000000000000";
        
        String input = amountStr + "|" + timestampStr + "|" + prevHashStr;
        
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var encodedhash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            var hexString = new StringBuilder();
            for (byte b : encodedhash) {
                var hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            this.currentHash = hexString.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SHA-256 hash for ledger entry", e);
        }
    }
}
