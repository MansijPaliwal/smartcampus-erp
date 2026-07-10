package com.smartcampus.erp.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Chunks a raw text document (such as rulebooks or syllabi) and saves its embeddings to the Vector Database.
     *
     * @param content  The raw text content of the document.
     * @param metadata Meta-attributes such as document source, department, or date.
     */
    public void ingestDocument(String content, Map<String, Object> metadata) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Configure text splitter with appropriate chunk size and overlap
        TokenTextSplitter splitter = new TokenTextSplitter(600, 120, 10, 5000, true);
        
        Document rawDocument = new Document(content, metadata);
        List<Document> splitDocuments = splitter.split(rawDocument);
        
        // This embeds and persists documents in the configured PgVectorStore / VectorStore
        vectorStore.accept(splitDocuments);
    }
}
