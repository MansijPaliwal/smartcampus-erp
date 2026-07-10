package com.smartcampus.erp.service;

public interface AiAdvisorService {
    String generateGpaStrategyInsight(Long studentUserId);
    String advise(String query);
}
