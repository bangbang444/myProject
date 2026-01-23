package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.OpeningHour;
import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, Long> {
    void deleteByRestaurant(Restaurant restaurant);
}
