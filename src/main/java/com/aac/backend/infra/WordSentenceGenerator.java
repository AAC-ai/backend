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
    private final PromptRouter promptRouter;
    private final int candidateCount;

    public WordSentenceGenerator(ChatClient chatClient,
                                 SentenceReranker sentenceReranker,
                                 ConversationContextSummarizer contextSummarizer,
                                 PromptRouter promptRouter,
                                 @Value("${generation.candidates:3}") int candidateCount) {
        this.chatClient = chatClient;
        this.sentenceReranker = sentenceReranker;
        this.contextSummarizer = contextSummarizer;
        this.promptRouter = promptRouter;
        this.candidateCount = candidateCount;
    }

    public GenerationResult generate(List<WordRequest> wordRequests, List<ConversationMessage> history) {
        var symbols = formatWords(wordRequests);
        log.info("symbols: {}", symbols);

        var summarizedPrompt = buildSystemPrompt(wordRequests, history, symbols);

        var start = System.currentTimeMillis();
        try {
            var response = chatClient.prompt()
                    .system(summarizedPrompt.prompt())
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
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens(),
                    summarizedPrompt.contextSummary()
            );
        } catch (Exception e) {
            var latencyMs = System.currentTimeMillis() - start;
            log.error("AI 문장 생성 실패: {}", e.getMessage());
            return GenerationResult.failure(null, latencyMs, e.getMessage());
        }
    }

    private SummarizedPrompt buildSystemPrompt(List<WordRequest> words, List<ConversationMessage> history, String symbols) {
        var basePrompt = promptRouter.route(words);
        var summary = contextSummarizer.summarize(history, symbols);
        if (summary.isEmpty()) {
            return new SummarizedPrompt(basePrompt, null);
        }
        var prompt = basePrompt + "\n\n현재 상황: " + summary.get() + "\n이 상황을 고려하여 문장을 생성하세요.";
        return new SummarizedPrompt(prompt, summary.get());
    }

    public String formatWords(List<WordRequest> words) {
        return words.stream()
                .map(w -> "[" + w.category() + "] " + w.label())
                .collect(Collectors.joining(", "));
    }

    private record SummarizedPrompt(String prompt, String contextSummary) {}
}
