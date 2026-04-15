package bangbang.gourmet.crawler;

import bangbang.gourmet.global.ncp.AddressResult;
import bangbang.gourmet.global.s3.S3Buckets;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.*;
import bangbang.gourmet.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static bangbang.gourmet.crawler.NaverMapConstants.Image.NAVER_REFERRER;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlDataPersistenceService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final OpeningHourRepository openingHourRepository;
    private final MenuRepository menuRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final S3Service s3Service;

    @Transactional
    public Long saveCrawledData(RestaurantCrawledDto dto) {
        // 1. 식당 조회 또는 생성
        Restaurant restaurant = restaurantRepository.findByNaverPlaceId(dto.getNaverPlaceId())
                .orElseGet(() -> restaurantRepository.save(Restaurant.builder()
                        .restaurantName(dto.getName())
                        .address(dto.getAddress())
                        .naverPlaceId(dto.getNaverPlaceId())
                        .build()));

        // 2. 기본 정보 업데이트 (좌표 등 최신화) - 필수!
        restaurant.updateInfo(dto.getLatitude(), dto.getLongitude());
        restaurant.updatePhoneNumber(dto.getPhoneNumber());

        // 3. 카테고리 처리 (기존 삭제 → saveAll로 새 데이터 저장)
        if (dto.getCategories() != null) {
            restaurantCategoryRepository.deleteByRestaurant(restaurant);
            restaurantCategoryRepository.flush();
            restaurant.getRestaurantCategories().clear();

            List<RestaurantCategory> newMappings = dto.getCategories().stream()
                    .distinct()
                    .map(catName -> { // TODO: N+1 고려해보기
                        Category category = categoryRepository.findByName(catName)
                                .orElseGet(() -> categoryRepository.save(new Category(catName)));
                        return RestaurantCategory.builder()
                                .restaurant(restaurant)
                                .category(category)
                                .build();
                    })
                    .toList();
            restaurantCategoryRepository.saveAll(newMappings);
        }

        // 4. 영업시간 처리 (기존 삭제 → saveAll, null이면 기존 데이터 유지)
        if (dto.getOpeningHours() != null) {
            openingHourRepository.deleteByRestaurant(restaurant);
            openingHourRepository.flush(); // TODO: 필요있는지 고려해보기
            restaurant.getOpeningHours().clear();

            List<OpeningHour> newHours = dto.getOpeningHours().stream()
                    .map(h -> OpeningHour.of(h, restaurant))
                    .toList();
            List<OpeningHour> savedHours = openingHourRepository.saveAll(newHours);
            restaurant.getOpeningHours().addAll(savedHours);
        }

        // 5. 메뉴 처리 (기존 삭제 → saveAll, null이면 기존 데이터 유지)
        if (dto.getMenus() != null) {
            menuRepository.deleteByRestaurant(restaurant);
            menuRepository.flush();
            restaurant.getMenus().clear();

            List<Menu> newMenus = dto.getMenus().stream()
                    .map(m -> Menu.builder()
                            .name(m.getName())
                            .price(m.getPrice())
                            .restaurant(restaurant)
                            .build())
                    .toList();
            List<Menu> savedMenus = menuRepository.saveAll(newMenus);
            restaurant.getMenus().addAll(savedMenus);
        }

        // 6. 썸네일 이미지 처리
        if (dto.getThumbnailUrl() != null) {
            restaurantImageRepository.findByRestaurantOrderByDisplayOrderAsc(restaurant)
                    .forEach(image -> s3Service.delete(S3Buckets.RESTAURANT, image.getImageUrl()));
            restaurantImageRepository.deleteByRestaurant(restaurant);
            try {
                String imageKey = s3Service.uploadImageFromUrl(dto.getThumbnailUrl(), S3Buckets.RESTAURANT, "thumbnails", NAVER_REFERRER);
                restaurantImageRepository.save(RestaurantImage.of(restaurant, imageKey, 0));
            } catch (Exception e) {
                log.warn("썸네일 업로드 실패 - 식당 데이터는 저장됨 (ID: {}): {}", restaurant.getRestaurantId(), e.getMessage());
            }
        }

        log.info("성공적으로 저장/업데이트 되었습니다: {}", restaurant.getRestaurantName());
        return restaurant.getRestaurantId();
    }

    @Transactional
    public void applyAddress(Long restaurantId, String address, String restaurantName, AddressResult result) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            String fullAddress = String.format("%s (%s)", address, restaurantName);
            restaurant.updateAddress(result.sido(), result.sigungu(), result.dong(), fullAddress);
        });
    }
}
