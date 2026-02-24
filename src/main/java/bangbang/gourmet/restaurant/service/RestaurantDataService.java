package bangbang.gourmet.restaurant.service;

import bangbang.gourmet.global.ai.CategoryClassifier;
import bangbang.gourmet.restaurant.entity.Menu;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantDataService {
    private final RestaurantRepository restaurantRepository;
    private final CategoryClassifier categoryClassifier;
    private final TransactionTemplate transactionTemplate; // 1. 주입받기

    public void updateMissingCategories() {
        List<Restaurant> targets = restaurantRepository.findByMainCategoryIsNull();
        log.info("업데이트 대상 식당 수: {}개", targets.size());

        for (Restaurant restaurant : targets) {
            try {
                // 2. 실제 DB 저장 로직만 트랜잭션으로 감싸기
                transactionTemplate.execute(status -> {
                    // 메뉴 이름 리스트 추출
                    List<String> menuNames = restaurant.getMenus().stream()
                            .map(Menu::getName)
                            .collect(Collectors.toList());

                    // 원본 카테고리 추출
                    String rawCategories = restaurant.getRestaurantCategories().stream()
                            .map(rc -> rc.getCategory().getName())
                            .collect(Collectors.joining(", "));

                    if (rawCategories.isEmpty()) rawCategories = "정보 없음";

                    log.info("[{}] 분류 요청 중...", restaurant.getRestaurantName());
                    String mainCategory = categoryClassifier.classify(
                            restaurant.getRestaurantName(),
                            rawCategories,
                            menuNames
                    );

                    // 결과 업데이트 및 저장
                    restaurant.updateMainCategory(mainCategory);
                    restaurantRepository.save(restaurant);
                    log.info("[{}] -> 완료: {}", restaurant.getRestaurantName(), mainCategory);

                    return null; // void 대신 null 반환
                });

                // 3. 트랜잭션이 종료된 후 "쉬는 시간" (DB 커넥션 반납 상태)
                Thread.sleep(7000);

            } catch (InterruptedException e) {
                log.error("업데이트 중단됨", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("[{}] 처리 중 오류 발생: {}", restaurant.getRestaurantName(), e.getMessage());
            }
        }
        log.info("모든 데이터 업데이트 프로세스 종료");
    }
}