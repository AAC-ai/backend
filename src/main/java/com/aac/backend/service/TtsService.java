package com.aac.backend.service;

import com.aac.backend.infra.clovavoice.ClovaVoiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TtsService {

    private final ClovaVoiceClient clovaVoiceClient;

    public byte[] synthesize(String text) {
        return clovaVoiceClient.synthesize(text);
    }
}
