package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.OpeningHour;
import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, Long> {
    @Modifying
    @Query("delete from OpeningHour o where o.restaurant = :restaurant")
    void deleteByRestaurant(@Param("restaurant") Restaurant restaurant);
}
