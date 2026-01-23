package bangbang.gourmet.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id") // 외래키 컬림 이름
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    public RestaurantCategory(Restaurant restaurant, Category category) {
        this.restaurant = restaurant;
        this.category = category;

        // 객체 지향적으로 양쪽 리스트에 자신을 추가해주는 로직
        if (restaurant != null && !restaurant.getRestaurantCategories().contains(this)) {
            restaurant.getRestaurantCategories().add(this);
        }
        if (category != null && !category.getRestaurantCategories().contains(this)) {
            category.getRestaurantCategories().add(this);
        }
    }

}
