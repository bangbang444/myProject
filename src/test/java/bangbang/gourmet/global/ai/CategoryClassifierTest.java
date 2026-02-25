package bangbang.gourmet.global.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryClassifierTest {
    @Autowired
    private CategoryClassifier categoryClassifier;

    @Test
    @DisplayName("식당 정보를 던지면 AI가 적절한 대분류를 반환해야 한다")
    void classify_success() {
        // given
        String restaurantName = "연돈";
        String rawCategory = "일식, 돈가스";
        List<String> menuNames = List.of("등심까스", "안심까스", "치즈까스");

        // when
        String result = categoryClassifier.classify(restaurantName, rawCategory, menuNames);

        // then
        assertThat(result).isEqualTo("일식");
    }

    @Test
    @DisplayName("카페 정보를 던지면 CAFE를 반환해야 한다")
    void classify_cafe() {
        // given
        String restaurantName = "스타벅스 강남점";
        String rawCategory = "카페 > 커피전문점";
        List<String> menuNames = List.of("아메리카노", "카페라떼", "자바칩 프라푸치노");

        // when
        String result = categoryClassifier.classify(restaurantName, rawCategory, menuNames);

        // then
        System.out.println("=== AI 분류 결과: " + result + " ===");
        assertThat(result).isEqualTo("카페");
    }
}