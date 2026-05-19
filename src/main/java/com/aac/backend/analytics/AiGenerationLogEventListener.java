package com.aac.backend.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGenerationLogEventListener {

    private final AiGenerationLogRepository aiGenerationLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    @Transactional
    public void handle(AiGenerationLogEvent event) {
        log.info("AI 생성 로그 저장 시작 - traceId: {}, thread: {}", event.traceId(), Thread.currentThread().getName());
        try {
            var words = event.words();
            var result = event.result();
            var selectedWords = words.stream()
                    .map(w -> "[" + w.category() + "] " + w.label())
                    .collect(Collectors.joining(", "));
            var contextMessages = serializeContext(event.contextMessages());

            var generationLog = result.success()
                    ? AiGenerationLog.success(event.traceId(), words.size(), selectedWords, result.model(), result.latencyMs(), contextMessages,
                            result.promptTokens(), result.completionTokens(), result.totalTokens(), result.contextSummary())
                    : AiGenerationLog.failure(event.traceId(), words.size(), selectedWords, result.model(), result.latencyMs(), contextMessages, result.failureReason());

            aiGenerationLogRepository.save(generationLog);
        } catch (Exception e) {
            log.error("AI 생성 로그 저장 실패 - traceId: {}", event.traceId(), e);
        }
    }

    private String serializeContext(List<com.aac.backend.domain.ConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        try {
            var list = messages.stream()
                    .map(m -> Map.of("userInput", m.getUserInput(), "aiResponse", m.getAiResponse()))
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("컨텍스트 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}
