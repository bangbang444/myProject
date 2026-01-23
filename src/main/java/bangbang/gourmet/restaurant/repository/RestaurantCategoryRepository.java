package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.entity.RestaurantCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {
    // @Modifying이 있어야 '삭제'나 '수정' 쿼리가 작동합니다.
    @Modifying
    @Query("delete from RestaurantCategory rc where rc.restaurant = :restaurant")
    void deleteByRestaurant(@Param("restaurant") Restaurant restaurant);
}
