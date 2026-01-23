package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByRestaurantNameAndAddress(String restaurantName, String address);
}
