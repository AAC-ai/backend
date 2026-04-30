package com.aac.backend.infra;

import com.aac.backend.presentation.dto.request.WordRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ai")
@SpringBootTest
@ActiveProfiles("local")
class WordSentenceGeneratorIntegrationTest {

    @Autowired
    private WordSentenceGenerator wordSentenceGenerator;

    @Test
    void 단어_목록을_자연스러운_문장으로_변환한다() {
        var words = List.of(
                new WordRequest("감정", "배고파"),
                new WordRequest("음식", "밥"),
                new WordRequest("행동", "먹고싶어")
        );

        var result = wordSentenceGenerator.generate(words);

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }

    @Test
    void 단일_단어도_문장으로_변환한다() {
        var words = List.of(new WordRequest("장소", "학교"));

        var result = wordSentenceGenerator.generate(words);

        System.out.println("생성된 문장: " + result);
        assertThat(result).isNotBlank();
    }
}
