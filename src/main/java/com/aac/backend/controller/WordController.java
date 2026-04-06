package com.aac.backend.controller;

import com.aac.backend.controller.dto.request.SentenceRequest;
import com.aac.backend.controller.dto.response.SentenceResponse;
import com.aac.backend.controller.dto.response.WordResponse;
import com.aac.backend.domain.Word;
import com.aac.backend.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    @GetMapping
    public ResponseEntity<List<WordResponse>> getWords(@RequestParam String category) {
        List<Word> words = wordService.getWords(category);

        return ResponseEntity.ok(words.stream()
                .map(WordResponse::from)
                .toList());
    }

    @PostMapping("/sentence")
    public ResponseEntity<SentenceResponse> generateSentence(@Valid @RequestBody SentenceRequest request) {
        String sentence = wordService.generateSentence(request);

        return ResponseEntity.ok(new SentenceResponse(sentence));
    }
}
