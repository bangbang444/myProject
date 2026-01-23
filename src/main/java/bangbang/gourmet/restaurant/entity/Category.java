package bangbang.gourmet.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    @Column(unique = true)
    private String name; // 예: "다이어트", "샐러드"

    // 양방향 매핑 추가 (mappedBy는 RestaurantCategory에 있는 필드명 'category'와 일치해야 함)
    @OneToMany(mappedBy = "category")
    private List<RestaurantCategory> restaurantCategories = new ArrayList<>();

    public Category(String name){
        this.name = name;
    }
}
