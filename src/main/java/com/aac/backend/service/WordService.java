package com.aac.backend.service;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.domain.ConversationMessageRepository;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.infra.WordSentenceGenerator;
import com.aac.backend.presentation.argumentresolver.LoginUser;
import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(ConversationContextProperties.class)
public class WordService {

    private final WordSentenceGenerator wordSentenceGenerator;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;
    private final ConversationContextProperties contextProperties;

    @Transactional
    public String generateSentence(List<WordRequest> words, LoginUser loginUser) {
        var history = loginUser.userId()
                .map(this::loadContext)
                .orElse(List.of());

        var sentence = wordSentenceGenerator.generate(words, history);

        loginUser.userId().ifPresent(id -> saveConversationHistory(id, words, sentence));

        return sentence;
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
