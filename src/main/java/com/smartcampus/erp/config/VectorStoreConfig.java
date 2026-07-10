package com.smartcampus.erp.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.profiles.active:h2}")
    private String activeProfile;

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        if ("mysql".equalsIgnoreCase(activeProfile) || "h2".equalsIgnoreCase(activeProfile)) {
            // Provide a simple in-memory fallback VectorStore for local testing/fallback profiles
            return new SimpleVectorStore(embeddingModel);
        }
        // PostgreSQL/pgvector integration configuration for production RAG pipeline
        return new PgVectorStore(jdbcTemplate, embeddingModel);
    }
}
