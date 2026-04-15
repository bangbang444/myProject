package bangbang.gourmet.crawler;

import bangbang.gourmet.global.ncp.NcpMapService;
import bangbang.gourmet.restaurant.entity.*;
import bangbang.gourmet.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverCrawlerService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final OpeningHourRepository openingHourRepository;
    private final MenuRepository menuRepository;

    private final NcpMapService ncpMapService;

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
        restaurant.updatePhoneNumber(dto.getPhoneNumber());

        // 3. 카테고리 처리 (기존 ID 수집 → 새 데이터 저장 → 기존 삭제)
        if (dto.getCategories() != null) {
            List<Long> oldCategoryIds = restaurantCategoryRepository.findIdsByRestaurant(restaurant);

            dto.getCategories().stream()
                    .distinct()
                    .forEach(catName -> {
                        Category category = categoryRepository.findByName(catName)
                                .orElseGet(() -> categoryRepository.save(new Category(catName)));
                        restaurantCategoryRepository.save(RestaurantCategory.builder()
                                .restaurant(restaurant)
                                .category(category)
                                .build());
                    });

            restaurantCategoryRepository.deleteAllByIdInBatch(oldCategoryIds);
            restaurant.getRestaurantCategories().clear();
        }

        // 4. 영업시간 처리 (기존 ID 수집 → 새 데이터 저장 → 기존 삭제, null이면 기존 데이터 유지)
        if (dto.getOpeningHours() != null) {
            List<Long> oldHourIds = openingHourRepository.findIdsByRestaurant(restaurant);
            openingHourRepository.saveAll(dto.getOpeningHours().stream()
                    .map(h -> OpeningHour.of(h, restaurant))
                    .toList());
            openingHourRepository.deleteAllByIdInBatch(oldHourIds);
            restaurant.getOpeningHours().clear();
        }

        // 5. 메뉴 처리 (기존 ID 수집 → 새 데이터 저장 → 기존 삭제, null이면 기존 데이터 유지)
        if (dto.getMenus() != null) {
            List<Long> oldMenuIds = menuRepository.findIdsByRestaurant(restaurant);
            menuRepository.saveAll(dto.getMenus().stream()
                    .map(m -> Menu.builder()
                            .name(m.getName())
                            .price(m.getPrice())
                            .restaurant(restaurant)
                            .build())
                    .toList());
            menuRepository.deleteAllByIdInBatch(oldMenuIds);
            restaurant.getMenus().clear();
        }

        // 6. 저장
        restaurantRepository.save(restaurant); // 명시적인 save() 호출은 생략 가능 (Transaction 종료 시 자동 반영)
        // 7. 주소 등록
        ncpMapService.reverseGeocode(
                restaurant.getRestaurantId(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
        log.info("성공적으로 저장/업데이트 되었습니다: {}", restaurant.getRestaurantName());
    }
}