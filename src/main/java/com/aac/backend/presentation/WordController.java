package com.aac.backend.presentation;

import com.aac.backend.presentation.argumentresolver.CurrentUser;
import com.aac.backend.presentation.argumentresolver.LoginUser;
import com.aac.backend.presentation.dto.request.SentenceRequest;
import com.aac.backend.presentation.dto.response.ApiResponse;
import com.aac.backend.presentation.dto.response.SentenceResponse;
import com.aac.backend.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    @PostMapping("/sentence")
    public ResponseEntity<ApiResponse<SentenceResponse>> generateSentence(
            @Valid @RequestBody SentenceRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        var sentence = wordService.generateSentence(request.words(), loginUser);
        return ResponseEntity.ok(ApiResponse.success(new SentenceResponse(sentence)));
    }
}
