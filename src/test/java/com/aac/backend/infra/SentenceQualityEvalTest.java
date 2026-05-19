package com.aac.backend.infra;

import com.aac.backend.presentation.dto.request.WordRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Tag("ai")
@SpringBootTest
class SentenceQualityEvalTest {

    @Autowired
    private WordSentenceGenerator wordSentenceGenerator;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

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
        // given
        var words = evalCase.words().stream()
                .map(w -> new WordRequest(w.category(), w.label()))
                .toList();

        // when
        var result = wordSentenceGenerator.generate(words, List.of());

        // then
        assertThat(result.success())
                .as("id=%d 생성 실패: %s", evalCase.id(), result.failureReason())
                .isTrue();

        var query = wordSentenceGenerator.formatWords(words);
        var evaluator = new RelevancyEvaluator(chatClientBuilder);
        var evalRequest = new EvaluationRequest(query, List.of(), result.sentence());
        var evalResponse = evaluator.evaluate(evalRequest);

        log.info("[id={}] symbols='{}' | generated='{}' | relevant={} | score={}",
                evalCase.id(), query, result.sentence(),
                evalResponse.isPass(), evalResponse.getScore());

        assertThat(evalResponse.isPass())
                .as("id=%d: 생성 문장이 입력 기호와 관련이 없음.\n  기호: %s\n  문장: %s",
                        evalCase.id(), query, result.sentence())
                .isTrue();
    }

    record EvalCase(
            int id,
            int symbolCount,
            List<WordEntry> words,
            String referenceAnswer
    ) {}

    record WordEntry(String category, String label) {}
}
