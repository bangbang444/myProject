package bangbang.gourmet.crawler;

import bangbang.gourmet.global.ncp.NcpMapService;
import bangbang.gourmet.restaurant.entity.Category;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaverCrawlerServiceTest {

    @InjectMocks
    private NaverCrawlerService naverCrawlerService;

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RestaurantCategoryRepository restaurantCategoryRepository;
    @Mock private OpeningHourRepository openingHourRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private NcpMapService ncpMapService;

    private Restaurant buildRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .restaurantName("테스트식당")
                .address("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(restaurant, "restaurantId", 1L);
        return restaurant;
    }

    private RestaurantCrawledDto.OpeningHourDto buildHourDto() {
        return RestaurantCrawledDto.OpeningHourDto.builder()
                .dayOfWeek("월").startTime("09:00").endTime("21:00").build();
    }

    private RestaurantCrawledDto.MenuDto buildMenuDto() {
        return RestaurantCrawledDto.MenuDto.builder()
                .name("김치찌개").price("8000원").build();
    }

    @Test
    @DisplayName("크롤링된 전화번호가 DB에 저장되어야 한다")
    void saveCrawledData_SavesPhoneNumber() {
        // given
        Restaurant restaurant = buildRestaurant();
        RestaurantCrawledDto dto = RestaurantCrawledDto.builder()
                .name("테스트식당").address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .latitude(37.1).longitude(127.1)
                .categories(List.of("한식"))
                .openingHours(List.of()).menus(List.of())
                .build();

        given(restaurantRepository.findByRestaurantNameAndAddress(any(), any())).willReturn(Optional.of(restaurant));
        given(categoryRepository.findByName("한식")).willReturn(Optional.of(new Category("한식")));
        given(restaurantCategoryRepository.saveAll(any())).willReturn(List.of());
        given(openingHourRepository.saveAll(any())).willReturn(List.of());
        given(menuRepository.saveAll(any())).willReturn(List.of());
        given(restaurantRepository.save(any())).willReturn(restaurant);

        // when
        naverCrawlerService.saveCrawledData(dto);

        // then
        assertThat(restaurant.getPhoneNumber()).isEqualTo("02-1234-5678");
    }

    @Test
    @DisplayName("openingHours가 null이면 기존 영업시간을 건드리지 않아야 한다")
    void saveCrawledData_NullOpeningHours_KeepsExistingHours() {
        // given
        Restaurant restaurant = buildRestaurant();
        RestaurantCrawledDto dto = RestaurantCrawledDto.builder()
                .name("테스트식당").address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .latitude(37.1).longitude(127.1)
                .openingHours(null).menus(null).categories(null)
                .build();

        given(restaurantRepository.findByRestaurantNameAndAddress(any(), any())).willReturn(Optional.of(restaurant));
        given(restaurantRepository.save(any())).willReturn(restaurant);

        // when
        naverCrawlerService.saveCrawledData(dto);

        // then
        verify(openingHourRepository, never()).deleteByRestaurant(any());
        verify(openingHourRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("menus가 null이면 기존 메뉴를 건드리지 않아야 한다")
    void saveCrawledData_NullMenus_KeepsExistingMenus() {
        // given
        Restaurant restaurant = buildRestaurant();
        RestaurantCrawledDto dto = RestaurantCrawledDto.builder()
                .name("테스트식당").address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .latitude(37.1).longitude(127.1)
                .openingHours(null).menus(null).categories(null)
                .build();

        given(restaurantRepository.findByRestaurantNameAndAddress(any(), any())).willReturn(Optional.of(restaurant));
        given(restaurantRepository.save(any())).willReturn(restaurant);

        // when
        naverCrawlerService.saveCrawledData(dto);

        // then
        verify(menuRepository, never()).deleteByRestaurant(any());
        verify(menuRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("재수집 시 기존 영업시간/메뉴를 삭제하고 새 데이터를 저장해야 한다")
    void saveCrawledData_Recrawl_ReplacesExistingData() {
        // given
        Restaurant restaurant = buildRestaurant();
        List<Long> oldHourIds = List.of(1L, 2L);
        List<Long> oldMenuIds = List.of(3L, 4L);

        RestaurantCrawledDto dto = RestaurantCrawledDto.builder()
                .name("테스트식당").address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .latitude(37.1).longitude(127.1)
                .categories(null)
                .openingHours(List.of(buildHourDto()))
                .menus(List.of(buildMenuDto()))
                .build();

        given(restaurantRepository.findByRestaurantNameAndAddress(any(), any())).willReturn(Optional.of(restaurant));
        given(openingHourRepository.saveAll(any())).willReturn(List.of());
        given(menuRepository.saveAll(any())).willReturn(List.of());
        given(restaurantRepository.save(any())).willReturn(restaurant);

        // when
        naverCrawlerService.saveCrawledData(dto);

        // then
        verify(openingHourRepository).deleteByRestaurant(restaurant);
        verify(openingHourRepository).saveAll(any());
        verify(menuRepository).deleteByRestaurant(restaurant);
        verify(menuRepository).saveAll(any());
    }
}