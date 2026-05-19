package com.aac.backend.infra;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.presentation.dto.request.WordRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Tag("ai")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SentenceQualityEvalTest {

    private static final String EVALUATOR_SYSTEM_PROMPT = """
            당신은 AAC(보완대체의사소통) 문장 품질 평가자입니다.
            발달장애인이 선택한 기호 목록을 바탕으로 AI가 생성한 문장이 적절한지 평가합니다.

            평가 기준:
            1. 모든 기호의 핵심 의도가 문장에 반영되어 있는가
            2. 자연스러운 표현을 위한 의미적 변환은 허용합니다
               예) [음식] 물 + [행동] 먹고싶어 → '마시고 싶어' (올바른 변환)
               예) [감정] 배고파 + [행동] 먹고싶어 → '배고파, 먹고 싶어' (올바른 변환)
            3. 문장이 1인칭 시점으로 자연스러운 한국어인가
            4. 기호의 의미 범위를 벗어난 새로운 개념을 추가하지 않았는가
               - 허용: 조사(랑, 이랑, 에서, 도, 을, 가 등), 어미, 자연스러운 연결 표현
               - 허용: 기호를 자연스럽게 연결하기 위한 최소한의 문법 요소
               - 불허: 기호에 없는 새로운 명사, 동사, 장소, 사람 등

            다음 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:
            {"pass": true, "score": 0.95, "reason": "평가 이유를 한 문장으로"}
            """;

    @Autowired
    private WordSentenceGenerator wordSentenceGenerator;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private final List<CaseResult> results = new ArrayList<>();

    static Stream<EvalCase> evalCases() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = SentenceQualityEvalTest.class
                .getClassLoader()
                .getResourceAsStream("sentence-eval-dataset.json");
        List<EvalCase> cases = mapper.readValue(is, new TypeReference<>() {});
        return cases.stream();
    }

    @ParameterizedTest(name = "[{index}] id={0} symbolCount={1}")
    @MethodSource("evalCases")
    void 생성된_문장이_입력_기호의_의도를_반영해야_한다(EvalCase evalCase) throws Exception {
        var words = evalCase.words().stream()
                .map(w -> new WordRequest(w.category(), w.label()))
                .toList();
        var symbols = wordSentenceGenerator.formatWords(words);
        var history = buildHistory(evalCase.history());

        var result = wordSentenceGenerator.generate(words, history);

        if (!result.success()) {
            results.add(CaseResult.failure(evalCase, symbols, result.failureReason()));
            assertThat(result.success())
                    .as("id=%d 생성 실패: %s", evalCase.id(), result.failureReason())
                    .isTrue();
            return;
        }

        var evalResult = evaluate(symbols, result.sentence());

        results.add(CaseResult.of(evalCase, symbols, result.sentence(), evalResult));

        log.info("[id={}] symbols='{}' | generated='{}' | pass={} | score={} | reason='{}'",
                evalCase.id(), symbols, result.sentence(),
                evalResult.pass(), evalResult.score(), evalResult.reason());

        assertThat(evalResult.pass())
                .as("id=%d: %s\n  기호: %s\n  문장: %s",
                        evalCase.id(), evalResult.reason(), symbols, result.sentence())
                .isTrue();
    }

    private List<ConversationMessage> buildHistory(List<HistoryEntry> entries) throws Exception {
        if (entries == null || entries.isEmpty()) return List.of();
        Constructor<ConversationMessage> ctor = ConversationMessage.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        var result = new ArrayList<ConversationMessage>();
        for (var entry : entries) {
            var msg = ctor.newInstance();
            ReflectionTestUtils.setField(msg, "userInput", entry.userInput());
            ReflectionTestUtils.setField(msg, "aiResponse", entry.aiResponse());
            result.add(msg);
        }
        return result;
    }

    private EvalResult evaluate(String symbols, String sentence) throws Exception {
        var userMessage = "입력 기호: " + symbols + "\n생성 문장: " + sentence;
        var response = chatClientBuilder.build()
                .prompt()
                .system(EVALUATOR_SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();

        var mapper = new ObjectMapper();
        return mapper.readValue(response, EvalResult.class);
    }

    @AfterAll
    void writeReport() throws Exception {
        long passCount = results.stream().filter(CaseResult::pass).count();
        double passRate = results.isEmpty() ? 0 : (double) passCount / results.size();
        double avgScore = results.stream()
                .filter(r -> r.score() != null)
                .mapToDouble(CaseResult::score)
                .average()
                .orElse(0);

        var bySymbolCount = results.stream()
                .collect(Collectors.groupingBy(r -> r.symbolCount))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey() + "개",
                        e -> {
                            var group = e.getValue();
                            long gPass = group.stream().filter(CaseResult::pass).count();
                            return Map.of(
                                    "total", group.size(),
                                    "pass", gPass,
                                    "fail", group.size() - gPass,
                                    "passRate", String.format("%.0f%%", (double) gPass / group.size() * 100)
                            );
                        },
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));

        var report = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "summary", Map.of(
                        "total", results.size(),
                        "pass", passCount,
                        "fail", results.size() - passCount,
                        "passRate", String.format("%.1f%%", passRate * 100),
                        "avgScore", String.format("%.3f", avgScore)
                ),
                "bySymbolCount", bySymbolCount,
                "cases", results
        );

        var reportDir = Path.of("build/reports/eval");
        Files.createDirectories(reportDir);
        var reportFile = reportDir.resolve("eval-result-" + LocalDateTime.now()
                .toString().replaceAll("[:.T]", "-") + ".json");

        var mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(reportFile.toFile(), report);

        log.info("\n========== 평가 리포트 ==========");
        log.info("총 케이스: {} | 통과: {} | 실패: {} | 통과율: {} | 평균 점수: {}",
                results.size(), passCount, results.size() - passCount,
                String.format("%.1f%%", passRate * 100), String.format("%.3f", avgScore));
        results.stream()
                .filter(r -> !r.pass())
                .forEach(r -> log.warn("  [FAIL] id={} reason='{}' symbols='{}' generated='{}'",
                        r.id(), r.reason(), r.symbols(), r.generatedSentence()));
        log.info("리포트 저장: {}", reportFile.toAbsolutePath());
        log.info("==================================");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record EvalCase(int id, int symbolCount, List<HistoryEntry> history, List<WordEntry> words, String referenceAnswer) {}

    record HistoryEntry(String userInput, String aiResponse) {}

    record WordEntry(String category, String label) {}

    record EvalResult(boolean pass, double score, String reason) {}

    record CaseResult(int id, int symbolCount, String symbols, String referenceAnswer,
                      String generatedSentence, boolean pass, Double score,
                      String reason, String failureReason) {

        static CaseResult of(EvalCase c, String symbols, String generated, EvalResult eval) {
            return new CaseResult(c.id(), c.symbolCount(), symbols, c.referenceAnswer(),
                    generated, eval.pass(), eval.score(), eval.reason(), null);
        }

        static CaseResult failure(EvalCase c, String symbols, String reason) {
            return new CaseResult(c.id(), c.symbolCount(), symbols, c.referenceAnswer(),
                    null, false, null, null, reason);
        }
    }
}
