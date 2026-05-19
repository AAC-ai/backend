package com.aac.backend.infra;

import com.aac.backend.domain.ConversationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConversationContextSummarizer {

    private static final String SYSTEM_PROMPT = """
            당신은 AAC(보완대체의사소통) 대화 맥락 분석가입니다.
            이전 대화 기록과 현재 사용자가 선택한 기호를 보고, 현재 기호가 이전 대화와 관련이 있는지 판단하세요.

            판단 기준:
            - 이전 대화에 장소가 등장하면, 현재 기호가 무엇이든 관련 있음 (장소 컨텍스트는 항상 유효)
            - 이전 대화의 함께 있는 사람, 진행 중인 상황 등이 현재 기호와 연결되면 관련 있음
            - 완전히 다른 주제로 전환된 경우 관련 없음
            - 이전에 표현한 욕구 자체는 상황 요약에 포함하지 마세요 (이미 전달된 의사)

            관련 있을 때 요약 형식: "현재 ~하는/~한 상황이다." (한 문장, 명사/장소/상황 위주로)
            예) 이전: 라면 먹고싶어 → 요약: "현재 라면을 먹고 싶다고 표현한 상황이다." (X)
                → 요약: "현재 식사 중인 상황이다." (O, 욕구가 아닌 상황으로)
            예) 이전: 학교 가고싶어 → 현재: 급식 먹고싶어 → "현재 학교에 있는 상황이다."
            예) 이전: 학교 가고싶어 → 현재: 먹고싶어 → "현재 학교에 있는 상황이다." (장소가 있으면 현재 기호와 무관하게 관련 있음)

            다음 JSON 형식으로만 응답하세요:
            {"related": true, "summary": "현재 학교에 있는 상황이다."}
            {"related": false, "summary": null}
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String summarizerModel;

    public ConversationContextSummarizer(ChatClient chatClient,
                                         ObjectMapper objectMapper,
                                         @Value("${generation.summarizer-model:gpt-4o-mini}") String summarizerModel) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.summarizerModel = summarizerModel;
    }

    public Optional<String> summarize(List<ConversationMessage> history, String currentSymbols) {
        if (history == null || history.isEmpty()) {
            return Optional.empty();
        }

        var historyText = history.stream()
                .map(m -> "사용자: " + m.getUserInput() + "\nAI: " + m.getAiResponse())
                .collect(Collectors.joining("\n"));

        var userMessage = "이전 대화:\n" + historyText + "\n\n현재 기호: " + currentSymbols;

        try {
            var response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .options(OpenAiChatOptions.builder().model(summarizerModel).build())
                    .call()
                    .content();

            var result = objectMapper.readValue(response, SummaryResult.class);

            if (result.related() && result.summary() != null) {
                log.info("컨텍스트 요약: '{}'", result.summary());
                return Optional.of(result.summary());
            } else {
                log.info("컨텍스트 무관 — 이전 대화 배제");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("컨텍스트 요약 실패, 배제 처리: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private record SummaryResult(boolean related, String summary) {}
}
