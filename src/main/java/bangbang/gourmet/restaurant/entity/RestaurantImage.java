package bangbang.gourmet.restaurant.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String imageUrl;
    private Integer displayOrder;

    public static RestaurantImage of(Restaurant restaurant, String imageKey, int displayOrder) {
        RestaurantImage image = new RestaurantImage();
        image.restaurant = restaurant;
        image.imageUrl = imageKey;
        image.displayOrder = displayOrder;
        return image;
    }
}
