package bangbang.gourmet.restaurant.service;

import bangbang.gourmet.restaurant.dto.RestaurantSearchResponse;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("키워드가 포함된 식당 이름으로 검색하면 일치하는 결과 리스트가 반환된다")
    void searchByName_ReturnsMatchingRestaurants() {
        // given
        Restaurant r1 = Restaurant.builder()
                .restaurantName("맛있는 한식당")
                .mainCategory("한식")
                .dong("역삼동")
                .build();
        Restaurant r2 = Restaurant.builder()
                .restaurantName("한식 뷔페")
                .mainCategory("한식")
                .dong("선릉동")
                .build();

        given(restaurantRepository.findByRestaurantNameContainingIgnoreCase("한식"))
                .willReturn(List.of(r1, r2));

        // when
        List<RestaurantSearchResponse> result = restaurantService.searchByName("한식");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RestaurantSearchResponse::name)
                .containsExactly("맛있는 한식당", "한식 뷔페");
        assertThat(result).extracting(RestaurantSearchResponse::areaName)
                .containsExactly("역삼동", "선릉동");
    }

    @Test
    @DisplayName("키워드와 일치하는 식당이 없으면 빈 리스트를 반환한다")
    void searchByName_ReturnsEmptyList_WhenNoMatch() {
        // given
        given(restaurantRepository.findByRestaurantNameContainingIgnoreCase("없는식당"))
                .willReturn(List.of());

        // when
        List<RestaurantSearchResponse> result = restaurantService.searchByName("없는식당");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("식당 이름의 일부만 입력해도 포함된 결과가 반환된다")
    void searchByName_ReturnsResult_WhenPartialKeyword() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .restaurantName("맛있는 한식당")
                .mainCategory("한식")
                .dong("역삼동")
                .build();

        given(restaurantRepository.findByRestaurantNameContainingIgnoreCase("한식당"))
                .willReturn(List.of(restaurant));

        // when
        List<RestaurantSearchResponse> result = restaurantService.searchByName("한식당");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("맛있는 한식당");
    }

    @Test
    @DisplayName("이미지가 없는 식당은 thumbnailUrl이 null로 반환된다")
    void searchByName_ThumbnailNull_WhenNoImage() {
        // given
        Restaurant restaurant = Restaurant.builder()
                .restaurantName("이미지없는집")
                .mainCategory("양식")
                .dong("삼성동")
                .build();

        given(restaurantRepository.findByRestaurantNameContainingIgnoreCase("이미지없는집"))
                .willReturn(List.of(restaurant));

        // when
        List<RestaurantSearchResponse> result = restaurantService.searchByName("이미지없는집");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).thumbnailUrl()).isNull();
    }
}
