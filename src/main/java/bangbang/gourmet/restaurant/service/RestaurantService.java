package bangbang.gourmet.restaurant.service;

import bangbang.gourmet.restaurant.dto.RestaurantCrawledDto;
import bangbang.gourmet.restaurant.entity.Category;
import bangbang.gourmet.restaurant.entity.OpeningHour;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.entity.RestaurantCategory;
import bangbang.gourmet.restaurant.repository.CategoryRepository;
import bangbang.gourmet.restaurant.repository.OpeningHourRepository;
import bangbang.gourmet.restaurant.repository.RestaurantCategoryRepository;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final OpeningHourRepository openingHourRepository;

    @Transactional
    public void saveCrawledData(RestaurantCrawledDto dto) {
        // 1. 식당 조회 또는 생성
        Restaurant restaurant = restaurantRepository.findByRestaurantNameAndAddress(dto.getName(), dto.getAddress())
                .orElseGet(() -> restaurantRepository.save(Restaurant.builder()
                        .restaurantName(dto.getName())
                        .address(dto.getAddress())
                        .build()));


        // 2. 기본 정보 업데이트 (좌표 등 최신화) - 필수!
        restaurant.updateInfo(dto.getLatitude(), dto.getLongitude());

        // 3. 카테고리 처리
        if (dto.getCategories() != null) {
            restaurantCategoryRepository.deleteByRestaurant(restaurant);
            restaurantCategoryRepository.flush();
            restaurant.getRestaurantCategories().clear();

            // 새로운 매핑 리스트 생성
            dto.getCategories().stream()
                    .distinct()
                    .forEach(catName -> {
                        Category category = categoryRepository.findByName(catName)
                                .orElseGet(() -> categoryRepository.save(new Category(catName)));

                        RestaurantCategory mapping = RestaurantCategory.builder()
                                .restaurant(restaurant)
                                .category(category)
                                .build();

                        restaurantCategoryRepository.save(mapping);
                    });
        }

        // 4. 영업시간 처리 (명시적 삭제 및 저장)
        openingHourRepository.deleteByRestaurant(restaurant);
        openingHourRepository.flush(); // 삭제 확정
        restaurant.getOpeningHours().clear();

        if (dto.getOpeningHours() != null) {
            List<OpeningHour> newHours = dto.getOpeningHours().stream()
                    .map(h -> OpeningHour.of(h, restaurant))
                    .toList();
            openingHourRepository.saveAll(newHours);
        }

        // 5. 최종 저장 (영업시간은 CascadeType.ALL 설정으로 자동 저장됨)
        restaurantRepository.save(restaurant); // 명시적인 save() 호출은 생략 가능 (Transaction 종료 시 자동 반영)
        log.info("성공적으로 저장/업데이트 되었습니다: {}", restaurant.getRestaurantName());
    }
}