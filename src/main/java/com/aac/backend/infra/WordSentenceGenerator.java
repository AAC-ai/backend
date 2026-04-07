package com.aac.backend.infra;

import com.aac.backend.presentation.dto.request.WordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WordSentenceGenerator {

    private final ChatClient chatClient;

    public String generate(List<WordRequest> wordRequests) {
        var symbols = formatWords(wordRequests);
        log.info("symbols: {}", symbols);

        var response = chatClient.prompt()
                .system("""
                        당신은 발달장애인의 의사소통을 돕는 AAC 시스템입니다.
                        사용자가 선택한 기호 목록을 자연스러운 한국어 문장 1개로 변환하세요.
                        
                        규칙:
                        - 입력된 기호를 모두 포함해야 합니다.
                        - 기호를 임의로 추가하거나 생략하지 마세요.
                        - 기호 순서를 최대한 유지하세요.
                        - 문장은 1인칭 기준으로 작성하세요.
                        - 간결하고 자연스러운 구어체로 작성하세요.
                        - 문장만 출력하세요.
                        
                        예시:
                        입력: [감정] 배고파, [음식] 밥, [행동] 먹고싶어
                        출력: 나 배고파, 밥 먹고 싶어.
                        
                        입력: [장소] 학교, [행동] 가고싶어
                        출력: 나 학교 가고 싶어.
                        
                        입력: [음식] 물, [행동] 먹고싶어
                        출력: 나 물 마시고 싶어.
                        """)
                .user(symbols)
                .call()
                .chatResponse();

        var usage = response.getMetadata().getUsage();
        log.info("tokens - prompt: {}, completion: {}, total: {}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());

        return response.getResult().getOutput().getText();
    }

    private String formatWords(List<WordRequest> words) {
        return words.stream()
                .map(w -> "[" + w.category() + "] " + w.label())
                .collect(Collectors.joining(", "));
    }
}
