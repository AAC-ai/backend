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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {

    private static final int SESSION_EXPIRY_HOURS = 4;

    private final WordSentenceGenerator wordSentenceGenerator;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public String generateSentence(List<WordRequest> words, LoginUser loginUser) {
        var history = loginUser.userId()
                .filter(this::isSessionAlive)
                .map(conversationMessageRepository::findTop10ByUserIdOrderByCreatedAtAsc)
                .orElse(List.of());

        var sentence = wordSentenceGenerator.generate(words, history);

        loginUser.userId().ifPresent(id -> saveConversationHistory(id, words, sentence));

        return sentence;
    }

    private boolean isSessionAlive(Long userId) {
        var threshold = LocalDateTime.now().minusHours(SESSION_EXPIRY_HOURS);
        return conversationMessageRepository.existsByUserIdAndCreatedAtAfter(userId, threshold);
    }

    private void saveConversationHistory(Long userId, List<WordRequest> words, String sentence) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        var userInput = wordSentenceGenerator.formatWords(words);
        conversationMessageRepository.save(ConversationMessage.create(user, userInput, sentence));
    }
}
