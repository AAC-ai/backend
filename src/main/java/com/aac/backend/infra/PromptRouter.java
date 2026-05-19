package com.aac.backend.infra;

import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PromptRouter {

    private final String standardPrompt;
    private final String singleSymbolPrompt;

    public PromptRouter(@Value("${prompts.sentence}") String standardPrompt,
                        @Value("${prompts.sentence-single}") String singleSymbolPrompt) {
        this.standardPrompt = standardPrompt;
        this.singleSymbolPrompt = singleSymbolPrompt;
    }

    public String route(List<WordRequest> words) {
        if (words.size() == 1) {
            log.info("프롬프트 라우팅: single-symbol (기호 1개)");
            return singleSymbolPrompt;
        }
        log.info("프롬프트 라우팅: standard (기호 {}개)", words.size());
        return standardPrompt;
    }
}
