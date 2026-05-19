package com.aac.backend.infra;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WordSentenceGenerator {

    private final ChatClient chatClient;
    private final SentenceReranker sentenceReranker;
    private final ConversationContextSummarizer contextSummarizer;
    private final String systemPrompt;
    private final int candidateCount;

    public WordSentenceGenerator(ChatClient chatClient,
                                 SentenceReranker sentenceReranker,
                                 ConversationContextSummarizer contextSummarizer,
                                 @Value("${prompts.sentence}") String systemPrompt,
                                 @Value("${generation.candidates:3}") int candidateCount) {
        this.chatClient = chatClient;
        this.sentenceReranker = sentenceReranker;
        this.contextSummarizer = contextSummarizer;
        this.systemPrompt = systemPrompt;
        this.candidateCount = candidateCount;
    }

    public GenerationResult generate(List<WordRequest> wordRequests, List<ConversationMessage> history) {
        var symbols = formatWords(wordRequests);
        log.info("symbols: {}", symbols);

        var contextualPrompt = buildSystemPrompt(history, symbols);

        var start = System.currentTimeMillis();
        try {
            var response = chatClient.prompt()
                    .system(contextualPrompt)
                    .user(symbols)
                    .options(OpenAiChatOptions.builder().N(candidateCount).build())
                    .call()
                    .chatResponse();

            var latencyMs = System.currentTimeMillis() - start;
            var model = response.getMetadata().getModel();
            var usage = response.getMetadata().getUsage();

            var candidates = response.getResults().stream()
                    .map(g -> g.getOutput().getText())
                    .toList();

            log.info("model: {}, latency: {}ms, tokens - prompt: {}, completion: {}, total: {}, candidates: {}",
                    model, latencyMs,
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens(),
                    candidates);

            var best = sentenceReranker.rerank(candidates, symbols);

            return GenerationResult.success(
                    best, model, latencyMs,
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens()
            );
        } catch (Exception e) {
            var latencyMs = System.currentTimeMillis() - start;
            log.error("AI 문장 생성 실패: {}", e.getMessage());
            return GenerationResult.failure(null, latencyMs, e.getMessage());
        }
    }

    private String buildSystemPrompt(List<ConversationMessage> history, String symbols) {
        var summary = contextSummarizer.summarize(history, symbols);
        if (summary.isEmpty()) {
            return systemPrompt;
        }
        return systemPrompt + "\n\n현재 상황: " + summary.get() + "\n이 상황을 고려하여 문장을 생성하세요.";
    }

    public String formatWords(List<WordRequest> words) {
        return words.stream()
                .map(w -> "[" + w.category() + "] " + w.label())
                .collect(Collectors.joining(", "));
    }
}
