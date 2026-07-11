package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.entity.AssignmentSubmission;
import com.smartcampus.erp.repository.AssignmentSubmissionRepository;
import com.smartcampus.erp.service.ProctoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProctoringServiceImpl implements ProctoringService {

    private final AssignmentSubmissionRepository submissionRepository;

    public ProctoringServiceImpl(AssignmentSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public void analyzeSubmissionIntegrity(AssignmentSubmission submission) {
        if (submission == null) {
            return;
        }

        var score = 100;

        // 1. Check for rapid completion anomaly (threshold: 30 seconds)
        if (submission.getCompletionTimeSeconds() != null && submission.getCompletionTimeSeconds() < 30) {
            score -= 35; // Deduct for suspiciously fast completion
        }

        // 2. Check for IP address mismatch anomaly
        if (submission.getIpAddress() != null && submission.getStudent() != null) {
            var studentId = submission.getStudent().getId();
            var otherSubmissions = submissionRepository.findByStudentId(studentId);

            // Filter out the current submission from comparison if it's already saved
            var priorIps = otherSubmissions.stream()
                    .filter(s -> !s.getId().equals(submission.getId()))
                    .map(AssignmentSubmission::getIpAddress)
                    .filter(ip -> ip != null && !ip.trim().isEmpty())
                    .distinct()
                    .toList();

            // If student has established history of IPs, and the current IP is entirely new, flag as mismatch
            if (!priorIps.isEmpty() && !priorIps.contains(submission.getIpAddress())) {
                score -= 30; // Deduct for unexplained IP transition/device change
            }
        }

        // Ensure score bounds
        score = Math.max(0, Math.min(100, score));
        submission.setIntegrityScore(score);

        submissionRepository.save(submission);
    }
}
