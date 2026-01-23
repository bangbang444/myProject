package bangbang.gourmet.restaurant.entity;

import bangbang.gourmet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder // 1. 빌더 패턴 사용을 가능하게 함
@AllArgsConstructor // 2. 빌더가 필요로 하는 전체 생성자 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<RestaurantCategory> restaurantCategories = new ArrayList<>();

    private double averageRating;

    private int reviewCount;

    private double latitude;

    private double longitude;

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<OpeningHour> openingHours = new ArrayList<>();

    public void updateInfo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
