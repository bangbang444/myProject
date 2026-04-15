package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {
    List<RestaurantImage> findByRestaurantOrderByDisplayOrderAsc(Restaurant restaurant);
    void deleteByRestaurant(Restaurant restaurant);
}
