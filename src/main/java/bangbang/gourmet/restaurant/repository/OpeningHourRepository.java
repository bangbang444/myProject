package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.OpeningHour;
import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, Long> {
    @Query("select o.id from OpeningHour o where o.restaurant = :restaurant")
    List<Long> findIdsByRestaurant(@Param("restaurant") Restaurant restaurant);
}
