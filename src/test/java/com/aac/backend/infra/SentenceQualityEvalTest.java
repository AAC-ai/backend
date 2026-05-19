package com.aac.backend.infra;

import com.aac.backend.presentation.dto.request.WordRequest;
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
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
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
    void 생성된_문장이_입력_기호와_관련성이_있어야_한다(EvalCase evalCase) {
        var words = evalCase.words().stream()
                .map(w -> new WordRequest(w.category(), w.label()))
                .toList();
        var query = wordSentenceGenerator.formatWords(words);

        var result = wordSentenceGenerator.generate(words, List.of());

        if (!result.success()) {
            results.add(CaseResult.failure(evalCase, query, result.failureReason()));
            assertThat(result.success())
                    .as("id=%d 생성 실패: %s", evalCase.id(), result.failureReason())
                    .isTrue();
            return;
        }

        var evaluator = new RelevancyEvaluator(chatClientBuilder);
        var evalRequest = new EvaluationRequest(query, List.of(), result.sentence());
        var evalResponse = evaluator.evaluate(evalRequest);

        results.add(CaseResult.of(evalCase, query, result.sentence(), evalResponse.isPass(), evalResponse.getScore()));

        log.info("[id={}] symbols='{}' | generated='{}' | pass={} | score={}",
                evalCase.id(), query, result.sentence(), evalResponse.isPass(), evalResponse.getScore());

        assertThat(evalResponse.isPass())
                .as("id=%d: 생성 문장이 입력 기호와 관련이 없음.\n  기호: %s\n  문장: %s",
                        evalCase.id(), query, result.sentence())
                .isTrue();
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
                .forEach(r -> log.warn("  [FAIL] id={} symbols='{}' generated='{}'",
                        r.id(), r.symbols(), r.generatedSentence()));
        log.info("리포트 저장: {}", reportFile.toAbsolutePath());
        log.info("==================================");
    }

    record EvalCase(int id, int symbolCount, List<WordEntry> words, String referenceAnswer) {}

    record WordEntry(String category, String label) {}

    record CaseResult(int id, int symbolCount, String symbols, String referenceAnswer,
                      String generatedSentence, boolean pass, Double score, String failureReason) {

        static CaseResult of(EvalCase c, String symbols, String generated, boolean pass, double score) {
            return new CaseResult(c.id(), c.symbolCount(), symbols, c.referenceAnswer(),
                    generated, pass, score, null);
        }

        static CaseResult failure(EvalCase c, String symbols, String reason) {
            return new CaseResult(c.id(), c.symbolCount(), symbols, c.referenceAnswer(),
                    null, false, null, reason);
        }
    }
}
