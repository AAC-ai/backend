package com.aac.backend.infra;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WordSentenceGenerator {

    private final ChatClient chatClient;
    private final String systemPrompt;

    public WordSentenceGenerator(ChatClient chatClient,
                                 @Value("${prompts.sentence}") String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    public GenerationResult generate(List<WordRequest> wordRequests, List<ConversationMessage> history) {
        var symbols = formatWords(wordRequests);
        log.info("symbols: {}", symbols);

        var messages = new ArrayList<Message>();
        for (ConversationMessage msg : history) {
            messages.add(new UserMessage(msg.getUserInput()));
            messages.add(new AssistantMessage(msg.getAiResponse()));
        }

        var start = System.currentTimeMillis();
        try {
            var response = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(messages)
                    .user(symbols)
                    .call()
                    .chatResponse();

            var latencyMs = System.currentTimeMillis() - start;
            var model = response.getMetadata().getModel();
            var usage = response.getMetadata().getUsage();
            log.info("model: {}, latency: {}ms, tokens - prompt: {}, completion: {}, total: {}",
                    model, latencyMs,
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens());

            return GenerationResult.success(
                    response.getResult().getOutput().getText(), model, latencyMs,
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens()
            );
        } catch (Exception e) {
            var latencyMs = System.currentTimeMillis() - start;
            log.error("AI 문장 생성 실패: {}", e.getMessage());
            return GenerationResult.failure(null, latencyMs, e.getMessage());
        }
    }

    public String formatWords(List<WordRequest> words) {
        return words.stream()
                .map(w -> "[" + w.category() + "] " + w.label())
                .collect(Collectors.joining(", "));
    }
}
