package com.aac.backend.analytics;

import com.aac.backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ai_generation_logs")
public class AiGenerationLog extends BaseEntity {

    @Id
    private String traceId;

    private Integer wordCount;

    @Column(columnDefinition = "TEXT")
    private String selectedWords;

    private String model;

    private Long latencyMs;

    @Column(columnDefinition = "TEXT")
    private String contextMessages;

    private Boolean success;

    private String failureReason;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private AiGenerationLog(String traceId, Integer wordCount, String selectedWords,
                             String model, Long latencyMs, String contextMessages,
                             Boolean success, String failureReason,
                             Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        this.traceId = traceId;
        this.wordCount = wordCount;
        this.selectedWords = selectedWords;
        this.model = model;
        this.latencyMs = latencyMs;
        this.contextMessages = contextMessages;
        this.success = success;
        this.failureReason = failureReason;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public static AiGenerationLog success(String traceId, Integer wordCount, String selectedWords,
                                          String model, Long latencyMs, String contextMessages,
                                          Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        return new AiGenerationLog(traceId, wordCount, selectedWords, model, latencyMs, contextMessages,
                true, null, promptTokens, completionTokens, totalTokens);
    }

    public static AiGenerationLog failure(String traceId, Integer wordCount, String selectedWords,
                                          String model, Long latencyMs, String contextMessages, String failureReason) {
        return new AiGenerationLog(traceId, wordCount, selectedWords, model, latencyMs, contextMessages,
                false, failureReason, null, null, null);
    }
}
