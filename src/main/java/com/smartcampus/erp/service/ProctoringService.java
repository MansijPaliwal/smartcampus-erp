package com.smartcampus.erp.service;

import com.smartcampus.erp.entity.AssignmentSubmission;

public interface ProctoringService {
    /**
     * Examines submission parameters for potential cheating anomalies
     * (e.g., rapid execution/completion duration, geographical or IP discrepancies)
     * and assigns an integrity score to the record.
     *
     * @param submission The AssignmentSubmission model record.
     */
    void analyzeSubmissionIntegrity(AssignmentSubmission submission);
}
