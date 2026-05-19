package com.aac.backend.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
public class SentenceReranker {

    private static final String SYSTEM_PROMPT = """
            당신은 AAC(보완대체의사소통) 문장 선택 전문가입니다.
            발달장애인이 선택한 기호 목록에 대해 생성된 후보 문장들 중 가장 적절한 것을 고르세요.

            선택 기준:
            - 모든 기호의 의도가 빠짐없이 자연스럽게 반영되어 있는가
            - 발달장애인이 실제로 사용하기 좋은 간결하고 자연스러운 구어체인가
            - 기호에 없는 내용을 임의로 추가하지 않았는가

            선택한 후보의 번호만 숫자로 응답하세요. 다른 텍스트는 포함하지 마세요. (예: 1)
            """;

    private final ChatClient chatClient;

    public SentenceReranker(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String rerank(List<String> candidates, String symbols) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        var candidateText = IntStream.range(0, candidates.size())
                .mapToObj(i -> (i + 1) + ". " + candidates.get(i))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        var userMessage = "기호: " + symbols + "\n\n후보:\n" + candidateText;

        try {
            var response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content()
                    .trim();

            var index = Integer.parseInt(response) - 1;
            if (index >= 0 && index < candidates.size()) {
                log.info("리랭크: {}번 선택 - '{}'", index + 1, candidates.get(index));
                return candidates.get(index);
            }
        } catch (Exception e) {
            log.warn("리랭크 실패, 첫 번째 후보 사용: {}", e.getMessage());
        }

        return candidates.get(0);
    }
}
