package com.aac.backend.service;

import com.aac.backend.domain.Word;
import com.aac.backend.domain.WordRepository;
import com.aac.backend.controller.dto.request.SentenceRequest;
import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;

    public List<Word> getWords(String categoryName) {
        return wordRepository.findByCategory_Name(categoryName);
    }

    public String generateSentence(SentenceRequest request) {
        return request.wordIds().stream()
                .map(id -> wordRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.WORD_NOT_FOUND)))
                .map(Word::getLabel)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }
}
