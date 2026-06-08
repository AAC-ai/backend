package com.aac.backend.infra.clovavoice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova-voice")
public record ClovaVoiceProperties(String apiKeyId, String apiKey, String url) {
}
