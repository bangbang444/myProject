package bangbang.gourmet.restaurant.service;

import bangbang.gourmet.restaurant.dto.RestaurantResponse;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    public static final double RANGE = 0.02;
    public static final int EARTH_RADIUS_KM = 6371;
    private final RestaurantRepository restaurantRepository;

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


}
