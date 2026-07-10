package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.GpaResponse;
import com.smartcampus.erp.service.AiAdvisorService;
import com.smartcampus.erp.service.GpaService;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiAdvisorServiceImpl implements AiAdvisorService {

    private final GpaService gpaService;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public AiAdvisorServiceImpl(GpaService gpaService, ChatModel chatModel, VectorStore vectorStore) {
        this.gpaService = gpaService;
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @Override
    public String generateGpaStrategyInsight(Long studentUserId) {
        GpaResponse gpaData = gpaService.calculateGpa(studentUserId);
        
        String structuralContext = gpaData.getCourseGrades().stream()
                .map(c -> String.format("Course %s: %s (Score: %s%%)", c.getCourseCode(), c.getCourseTitle(), c.getPercentage()))
                .collect(Collectors.joining(", "));

        String promptStr = String.format(
                "You are an elite academic performance advisor. A student has a Cumulative GPA of %.2f. " +
                "Their specific course breakdown performance includes: [%s]. " +
                "Generate a highly tactical 3-sentence study optimization blueprint focusing on weakness areas. " +
                "Do not use generic fluff. Be direct and encouraging.",
                gpaData.getGpa(), structuralContext
        );

        try {
            return chatModel.call(promptStr);
        } catch (Exception e) {
            e.printStackTrace();
            return String.format(
                "Your Cumulative GPA is currently %.2f. To maximize your academic potential, focus on allocating structured study blocks for your lower-percentile courses and review exam keys carefully. We also recommend scheduling regular sessions with course instructors during office hours to solidify complex subject areas.",
                gpaData.getGpa()
            );
        }
    }

    @Override
    public String advise(String query) {
        try {
            // Retrieve contextual documents using vector store similarity search
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.query(query)
                            .withTopK(4)
                            .withSimilarityThreshold(0.5)
            );
            
            String context = documents.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n"));
            
            // Build RAG-specific instructions forcing the LLM to ground replies in context only
            String systemInstruction = 
                    "You are the official academic and campus advisor for SmartCampus ERP.\n" +
                    "Use ONLY the following context to answer the student's question.\n" +
                    "If the answer is not contained in the context, say: \"I'm sorry, I cannot find that information in our campus rulebooks or syllabi.\"\n" +
                    "Do not make up facts or hallucinate answers outside this context.\n\n" +
                    "Context:\n" +
                    context;
            
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemInstruction),
                    new UserMessage(query)
            ));
            
            return chatModel.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return "I am currently unable to access the vector knowledge store to advise you. Please consult the registrar's office.";
        }
    }
}
