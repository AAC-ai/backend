package com.aac.backend.service;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.domain.ConversationMessageRepository;
import com.aac.backend.domain.User;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import com.aac.backend.infra.WordSentenceGenerator;
import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {

    private static final Long FIXED_USER_ID = 1L;

    private final WordSentenceGenerator wordSentenceGenerator;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;

    public String generateSentence(List<WordRequest> words) {
        User user = userRepository.findById(FIXED_USER_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<ConversationMessage> history =
                conversationMessageRepository.findTop10ByUserIdOrderByCreatedAtAsc(FIXED_USER_ID);

        String sentence = wordSentenceGenerator.generate(words, history);

        String userInput = wordSentenceGenerator.formatWords(words);
        conversationMessageRepository.save(ConversationMessage.create(user, userInput, sentence));

        return sentence;
    }
}
