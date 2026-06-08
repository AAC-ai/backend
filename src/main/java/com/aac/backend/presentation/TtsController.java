package com.aac.backend.presentation;

import com.aac.backend.presentation.dto.request.TtsRequest;
import com.aac.backend.service.TtsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    @PostMapping
    public ResponseEntity<byte[]> synthesize(
            @Valid @RequestBody TtsRequest request
    ) {
        byte[] audio = ttsService.synthesize(request.text());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(audio);
    }
}
