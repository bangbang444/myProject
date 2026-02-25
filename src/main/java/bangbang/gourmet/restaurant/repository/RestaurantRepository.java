package bangbang.gourmet.restaurant.repository;

import bangbang.gourmet.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByRestaurantNameAndAddress(String restaurantName, String address);

    List<Restaurant> findByMainCategoryIsNull();

    @Query(value = "SELECT * FROM restaurant " +
            "WHERE latitude BETWEEN :minLat AND :maxLat " +
            "AND longitude BETWEEN :minLon AND :maxLon", nativeQuery = true)
    List<Restaurant> findByLocationRange(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                         @Param("minLon") double minLon, @Param("maxLon") double maxLon);


    @Query("select r from Restaurant r left join fetch r.imageUrls where r.restaurantId = :id")
    Optional<Restaurant> findByIdWithImages(@Param("id") Long id);
}