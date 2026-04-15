package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Menu;
import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("select m.id from Menu m where m.restaurant = :restaurant")
    List<Long> findIdsByRestaurant(@Param("restaurant") Restaurant restaurant);
}
