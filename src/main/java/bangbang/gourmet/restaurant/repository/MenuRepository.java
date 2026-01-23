package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Menu;
import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    void deleteByRestaurant(Restaurant restaurant);
}
