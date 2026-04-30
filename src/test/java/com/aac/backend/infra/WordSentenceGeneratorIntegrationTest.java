package com.aac.backend.infra;

import com.aac.backend.domain.ConversationMessage;
import com.aac.backend.domain.User;
import com.aac.backend.domain.UserRepository;
import com.aac.backend.presentation.dto.request.WordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ai")
@SpringBootTest
@ActiveProfiles("local")
@Transactional
class WordSentenceGeneratorIntegrationTest {

    @Autowired
    private WordSentenceGenerator wordSentenceGenerator;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.create("test@aac.com", "테스트유저"));
    }

    @Test
    void 단어_목록을_자연스러운_문장으로_변환한다() {
        var words = List.of(
                new WordRequest("감정", "배고파"),
                new WordRequest("음식", "밥"),
                new WordRequest("행동", "먹고싶어")
        );

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 생성된_문장이_입력_기호와_관련성이_높다() {
        var words = List.of(
                new WordRequest("감정", "배고파"),
                new WordRequest("음식", "밥"),
                new WordRequest("행동", "먹고싶어")
        );
        var userInput = wordSentenceGenerator.formatWords(words);
        var sentence = wordSentenceGenerator.generate(words, List.of());

        var evaluator = new RelevancyEvaluator(chatClientBuilder);
        var response = evaluator.evaluate(new EvaluationRequest(userInput, sentence));

        System.out.println("생성된 문장: " + sentence);
        System.out.println("관련성 평가 결과: " + response.isPass() + ", 점수: " + response.getScore());
        assertThat(response.isPass()).isTrue();
    }

    @Test
    void 단일_단어도_문장으로_변환한다() {
        var words = List.of(new WordRequest("장소", "학교"));

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 단어_10개_이상_조합도_문장으로_변환한다() {
        var words = List.of(
                new WordRequest("감정", "배고파"),
                new WordRequest("감정", "피곤해"),
                new WordRequest("음식", "밥"),
                new WordRequest("음식", "물"),
                new WordRequest("장소", "집"),
                new WordRequest("장소", "학교"),
                new WordRequest("행동", "가고싶어"),
                new WordRequest("행동", "먹고싶어"),
                new WordRequest("행동", "자고싶어"),
                new WordRequest("사람", "엄마"),
                new WordRequest("사람", "선생님")
        );

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 모순된_단어_여러개_조합도_문장을_생성한다() {
        var words = List.of(
                new WordRequest("행동", "가고싶어"),
                new WordRequest("행동", "집에있고싶어"),
                new WordRequest("감정", "좋아"),
                new WordRequest("감정", "싫어"),
                new WordRequest("장소", "병원"),
                new WordRequest("장소", "집")
        );

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 부정_감정_단어도_문장으로_변환한다() {
        var words = List.of(
                new WordRequest("신체", "머리"),
                new WordRequest("감정", "아파"),
                new WordRequest("행동", "싫어")
        );

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 컨텍스트가_없으면_단어만으로_문장을_생성한다() {
        var words = List.of(new WordRequest("감정", "무서워"));

        var result = wordSentenceGenerator.generate(words, List.of());

        System.out.println("컨텍스트 없음: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 컨텍스트가_있으면_이전_장소_정보를_반영한다() {
        var history = List.of(
                ConversationMessage.create(testUser, "[장소] 병원", "나 병원 가기 싫어.")
        );
        var words = List.of(new WordRequest("감정", "무서워"));

        var withContext = wordSentenceGenerator.generate(words, history);
        var withoutContext = wordSentenceGenerator.generate(words, List.of());

        System.out.println("컨텍스트 있음: " + withContext);
        System.out.println("컨텍스트 없음: " + withoutContext);

        // 컨텍스트 있을 때 병원 관련 맥락이 반영되는지 확인
        assertThat(withContext).isNotBlank();
        assertThat(withContext).containsIgnoringCase("병원");
    }
}
