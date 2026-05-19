package com.aac.backend.analytics;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.infra.GenerationResult;
import com.aac.backend.presentation.dto.request.WordRequest;

import java.util.List;

public record AiGenerationLogEvent(
        String traceId,
        List<WordRequest> words,
        List<ConversationMessage> contextMessages,
        GenerationResult result
) {

}
