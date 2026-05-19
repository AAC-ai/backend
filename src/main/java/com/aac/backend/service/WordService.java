package com.aac.backend.service;

import com.aac.backend.analytics.AiGenerationLogEvent;
import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.domain.ConversationMessageRepository;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.infra.GenerationResult;
import com.aac.backend.infra.WordSentenceGenerator;
import com.aac.backend.presentation.argumentresolver.LoginUser;
import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(ConversationContextProperties.class)
public class WordService {

    private final WordSentenceGenerator wordSentenceGenerator;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final ConversationContextProperties contextProperties;

    @Transactional
    public String generateSentence(List<WordRequest> words, LoginUser loginUser) {
        var history = loginUser.userId()
                .map(this::loadContext)
                .orElse(List.of());

        var traceId = UUID.randomUUID().toString();
        var result = wordSentenceGenerator.generate(words, history);

        eventPublisher.publishEvent(new AiGenerationLogEvent(traceId, words, history, result));

        if (!result.success()) {
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }

        loginUser.userId().ifPresent(id -> saveConversationHistory(id, words, result.sentence()));

        return result.sentence();
    }

    private List<ConversationMessage> loadContext(Long userId) {
        var threshold = LocalDateTime.now().minusHours(contextProperties.thresholdHours());
        return conversationMessageRepository.findRecentContext(
                userId, threshold, PageRequest.of(0, contextProperties.limit())
        );
    }

    private void saveConversationHistory(Long userId, List<WordRequest> words, String sentence) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        var userInput = wordSentenceGenerator.formatWords(words);
        conversationMessageRepository.save(ConversationMessage.create(user, userInput, sentence));
    }
}
