package bangbang.gourmet.global.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryClassifier {
    private final OpenAiChatModel openAiChatModel;

    /**
     * 식당 정보를 기반으로 대분류 카테고리를 판별합니다.
     */
    public String classify(String restaurantName, String rawCategory, List<String> menuNames) {
        // 1. 메뉴 리스트를 문자열로 결합 (최대 5개 정도면 충분합니다)
        String menus = menuNames.stream()
                .limit(5)
                .collect(Collectors.joining(", "));

        // 2. AI에게 보낼 지시사항 (System Prompt)
        String systemPrompt = """
                너는 한국의 외식 업계와 맛집 분류 체계에 정통한 전문가야.
                    제공되는 식당명, 원본 카테고리, 메뉴 리스트를 분석해서 가장 적합한 '대분류' 하나만 골라줘
            
                분류 리스트: [한식, 고기/구이, 일식, 중식, 양식, 아시안, 패스트푸드, 치킨, 분식, 카페, 주점, 기타]
                
                [분류 가이드 (반드시 준수)]
                1. 한식: 찌개, 국밥, 백반, 비빔밥, 생선구이, 찜닭, 닭도리탕, 냉면 등 (구워 먹는 고기 요리 제외)
                2. 고기/구이: 삼겹살, 돼지갈비, 소고기, 곱창, 양꼬치, 닭갈비 등 불판에 구워 먹는 육류 요리
                3. 일식: 초밥, 라멘, 돈가스, 소바, 텐동, 규동, 회 등 일본식 요리
                4. 중식: 짜장면, 짬뽕, 마라탕, 꿔바로우, 딤섬 등 중국식 요리
                5. 양식: 파스타, 스테이크, 이탈리안, 프랑스 요리, 멕시칸 등 서양식 요리
                6. 아시안: 쌀국수, 타이 요리, 인도 커리, 나시고랭 등 (일식/중식 제외 동남아 및 기타 아시아 요리)
                7. 패스트푸드: 햄버거, 피자, 샌드위치, 샐러드, 도넛 등 빠르고 간편한 서구식 식사
                8. 치킨: 프라이드, 양념치킨, 오븐구이 통닭, 닭강정 등 (주의: 닭갈비나 찜닭은 제외)
                9. 분식: 떡볶이, 김밥, 라면, 튀김, 어묵, 순대 등
                10. 카페: 커피, 디저트, 베이커리, 빙수, 전통찻집 등 음료와 간식 위주
                11. 주점: 이자카야, 요리주점, 와인바, 칵테일바, 맥주집, 포차 등 술 판매가 주 목적인 곳
                12. 기타: 위 11가지 분류에 속하지 않는 경우 (예: 편의점, 마트, 식자재 등)
                
                [답변 규칙]
                - 반드시 위 리스트에 있는 한글 단어 하나만 대답해.
                - 부가 설명, 마침표, 따옴표 없이 딱 해당 단어만 출력해.
                - 원본 카테고리 안 분류 리스트에 속하는게 있다면 그걸로 출력해. 겹치면 맨 앞 하나만 출력해
                - 예시: '결과: 한식' (X), '한식.' (X), '한식' (O)
            """;

        // 3. 실제 데이터 (User Message)
        String userMessageText = String.format("식당명: %s, 원본카테고리: %s, 대표메뉴: [%s]",
                restaurantName, rawCategory, menus);

        try {
            SystemMessage systemMessage = new SystemMessage(systemPrompt);
            UserMessage userMessage = new UserMessage(userMessageText);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            String response = openAiChatModel.call(prompt).getResult().getOutput().getText();
            return (response != null && !response.isBlank()) ? response.trim() : "기타";

        } catch (Exception e) {
            log.error("AI 카테고리 분류 중 오류 발생 (식당: {}): {}", restaurantName, e.getMessage());
            return "기타";
        }
    }
}
