package com.smartcampus.erp.service.impl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.smartcampus.erp.dto.GpaResponse;
import com.smartcampus.erp.service.AiAdvisorService;
import com.smartcampus.erp.service.GpaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiAdvisorServiceImpl implements AiAdvisorService {

    private final GpaService gpaService;
    private final OpenAIClient openAiClient;

    public AiAdvisorServiceImpl(
            GpaService gpaService,
            @Value("${OPENAI_API_KEY:mock-key}") String apiKey) {
        this.gpaService = gpaService;
        this.openAiClient = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }

    @Override
    public String generateGpaStrategyInsight(Long studentUserId) {
        GpaResponse gpaData = gpaService.calculateGpa(studentUserId);
        
        String structuralContext = gpaData.getCourseGrades().stream()
                .map(c -> String.format("Course %s: %s (Score: %s%%)", c.getCourseCode(), c.getCourseTitle(), c.getPercentage()))
                .collect(java.util.stream.Collectors.joining(", "));

        String prompt = String.format(
                "You are an elite academic performance advisor. A student has a Cumulative GPA of %.2f. " +
                "Their specific course breakdown performance includes: [%s]. " +
                "Generate a highly tactical 3-sentence study optimization blueprint focusing on weakness areas. " +
                "Do not use generic fluff. Be direct and encouraging.",
                gpaData.getGpa(), structuralContext
        );

        try {
            ChatCompletion completion = openAiClient.chat().completions().create(
                    ChatCompletionCreateParams.builder()
                            .model("gpt-4o-mini")
                            .addUserMessage(prompt)
                            .maxTokens(150L)
                            .temperature(0.7)
                            .build()
            );

            return completion.choices().get(0).message().content().orElse("Maintain consistent roadmap parameters.");
        } catch (Exception e) {
            e.printStackTrace();
            return String.format(
                "Your Cumulative GPA is currently %.2f. To maximize your academic potential, focus on allocating structured study blocks for your lower-percentile courses and review exam keys carefully. We also recommend scheduling regular sessions with course instructors during office hours to solidify complex subject areas.",
                gpaData.getGpa()
            );
        }
    }
}
