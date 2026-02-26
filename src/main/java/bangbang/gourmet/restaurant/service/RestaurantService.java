package bangbang.gourmet.restaurant.service;

import bangbang.gourmet.restaurant.dto.MenuResponse;
import bangbang.gourmet.restaurant.dto.OperatingHourResponse;
import bangbang.gourmet.restaurant.dto.RestaurantDetailResponse;
import bangbang.gourmet.restaurant.dto.RestaurantResponse;
import bangbang.gourmet.restaurant.entity.OpeningHour;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.entity.RestaurantImage;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewSimpleResponse;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;
import bangbang.gourmet.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class RestaurantService {
    public static final double RANGE = 0.02;
    public static final int EARTH_RADIUS_KM = 6371;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    public List<RestaurantResponse> getNearbyGems(double userLat, double userLon, int size) {
        // 위경도 ±0.02 정도의 범위를 계산해서 후보군만 가져옵니다.
        return restaurantRepository.findByLocationRange(userLat - RANGE, userLat + RANGE, userLon - RANGE, userLon + RANGE)
                .stream()
                .map(restaurant -> convertToResponse(restaurant, userLat, userLon))
                .sorted(Comparator.comparingDouble(RestaurantResponse::distance))
                .limit(size)
                .toList();
    }

    private RestaurantResponse convertToResponse(Restaurant restaurant, double userLat, double userLon) {
        double distance = calculateDistance(userLat, userLon, restaurant.getLatitude(), restaurant.getLongitude());
        return new RestaurantResponse(
                restaurant.getRestaurantId(),
                restaurant.getRestaurantName(),
                restaurant.getMainCategory(),
                restaurant.getDong(),
                restaurant.getAverageRating(),
                distance,
                ""
        );
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }


    // 상세 정보
    public RestaurantDetailResponse getRestaurantDetail(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        // 최신 리뷰 2개 조회 (PageRequest 사용)
        List<Review> recentReviews = reviewRepository.findTop2ByRestaurantIdWithUserAndImages(
                id, PageRequest.of(0, 2));

        return mapToDetailResponse(restaurant, recentReviews);
    }

    private RestaurantDetailResponse mapToDetailResponse(Restaurant restaurant, List<Review> reviews) {
        List<ReviewSimpleResponse> reviewResponses = reviews.stream()
                .map(r -> new ReviewSimpleResponse(
                        r.getId(),
                        r.getUser().getNickname(),
                        Math.round(r.getAverageRating() * 10) / 10.0,
                        r.getContent(),
                        r.getImages().stream().map(ReviewImage::getImageUrl).toList(),
                        formatCreatedAt(r.getCreatedDate())
                )).toList();


        List<OperatingHourResponse> operatingHours = restaurant.getOpeningHours().stream()
                .map(oh -> new OperatingHourResponse(
                        oh.getDayOfWeek(),
                        oh.getFormattedOperatingHours(),
                        oh.getBreakTime() != null ? oh.getBreakTime() : "-",
                        oh.getLastOrder() != null ? oh.getLastOrder() : "-"
                )).toList();

        List<MenuResponse> menuResponse = restaurant.getMenus().stream()
                .map(menu -> new MenuResponse(
                        menu.getName(),
                        menu.getPrice() != null ? menu.getPrice() : "가격 변동"
                )).toList();

        OpeningHour todayInfo = findTodayOpeningHour(restaurant);

        String todayOpeningHours = "정보 없음";
        String breakTime = "-";
        String lastOrder = "-";
        boolean isOpen = checkIsOpen(todayInfo);

        if (todayInfo != null) {
            todayOpeningHours = todayInfo.getFormattedOperatingHours();
            breakTime = todayInfo.getBreakTime() != null ? todayInfo.getBreakTime() : "-";
            lastOrder = todayInfo.getLastOrder() != null ? todayInfo.getLastOrder() : "-";
        }

        return new RestaurantDetailResponse(
                restaurant.getRestaurantId(),
                restaurant.getRestaurantName(),
                restaurant.getMainCategory(),
                restaurant.getSigungu(),
                restaurant.getAddressFull(),
                restaurant.getPhoneNumber() != null ? restaurant.getPhoneNumber() : "정보 없음",
                restaurant.getDescription() != null ? restaurant.getDescription() : "준비 중입니다.",
                todayOpeningHours,
                breakTime,
                lastOrder,
                isOpen,
                operatingHours,
                Math.round(restaurant.getAverageRating() * 10) / 10.0,
                restaurant.getReviewCount(),
                reviewResponses,
                restaurant.getImageUrls().stream().map(RestaurantImage::getImageUrl).toList(),
                menuResponse
        );
    }

    private String formatCreatedAt(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        if (duration.toDays() > 0) return duration.toDays() + "일 전";
        if (duration.toHours() > 0) return duration.toHours() + "시간 전";
        return "방금 전";
    }

    private OpeningHour findTodayOpeningHour(Restaurant restaurant) {
        // 현재 요일을 "월", "화", "수"... 형식의 한글로 변환
        String today = LocalDateTime.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        return restaurant.getOpeningHours().stream()
                .filter(oh -> oh.getDayOfWeek().equals(today) || oh.getDayOfWeek().equals("매일"))
                .findFirst()
                .orElse(null);
    }

    private boolean checkIsOpen(OpeningHour todayInfo) {
        if (todayInfo == null || todayInfo.getStartTime() == null || todayInfo.getEndTime() == null) {
            return false; // 정보가 없으면 영업 종료(false)로 처리
        }

        try {
            LocalTime now = LocalTime.now();
            String startStr = todayInfo.getStartTime();
            String endStr = todayInfo.getEndTime();

            LocalTime start = LocalTime.parse(startStr);
            // "24:00"이면 LocalTime.MAX(23:59:59.999)로 처리
            LocalTime end = endStr.equals("24:00") ? LocalTime.MAX : LocalTime.parse(endStr);

            // 현재 시간이 시작 시간과 종료 시간 사이에 있는지 확인
            // 영업 종료 시간이 시작 시간보다 빠르면 (예: 22:00 ~ 02:00), 다음 날까지 영업하는 경우
            if (end.isBefore(start)) {
                return !now.isBefore(start) || now.isBefore(end);
            }

            // 일반적인 경우 (예: 10:00 ~ 22:00)
            return !now.isBefore(start) && now.isBefore(end);
        } catch (DateTimeParseException e) {
            return false; // 시간 파싱 에러 시 안전하게 false 반환
        }
    }


}
