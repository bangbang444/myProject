package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.entity.RestaurantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {
    @Query("select rc.id from RestaurantCategory rc where rc.restaurant = :restaurant")
    List<Long> findIdsByRestaurant(@Param("restaurant") Restaurant restaurant);
}
