package com.aac.backend.presentation;

import com.aac.backend.infra.WordSentenceGenerator;
import com.aac.backend.presentation.dto.request.SentenceRequest;
import com.aac.backend.presentation.dto.response.SentenceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/words")
public class WordController {

    private final WordSentenceGenerator wordSentenceGenerator;

    @PostMapping("/sentence")
    public ResponseEntity<SentenceResponse> generateSentence(@Valid @RequestBody SentenceRequest request) {
        var sentence = wordSentenceGenerator.generate(request.words());

        return ResponseEntity.ok(new SentenceResponse(sentence));
    }
}
