package bangbang.gourmet.restaurant.entity;

import bangbang.gourmet.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
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

    @Version
    private Long version;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String address;

    @Column(unique = true)
    private String naverPlaceId;

    // 주소 필터링용 필드
    private String sido;
    private String sigungu;
    private String dong;
    private String addressFull;

    @OneToMany(mappedBy = "restaurant")
    @BatchSize(size = 20)
    @Builder.Default
    private final List<RestaurantImage> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<RestaurantCategory> restaurantCategories = new ArrayList<>();

    private double averageRating;

    private int reviewCount;

    private double latitude;

    private double longitude;

    private String mainCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String phoneNumber;

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<OpeningHour> openingHours = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<Menu> menus = new ArrayList<>();

    public void updateInfo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updateAddress(String sido, String sigungu, String dong, String addressFull) {
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
        this.addressFull = addressFull;
    }

    public void updateMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public void addReview(Double newRating){
        double totalRating = this.averageRating * this.reviewCount;
        this.reviewCount++;
        this.averageRating = (totalRating + newRating) / this.reviewCount;
    }

    public void decreaseReviewCount(Double rating) {
        if (this.reviewCount <= 0) return; // 방어 코드

        // 새로운 평균 = (기존 합계 - 삭제될 점수) / (기존 개수 - 1)
        if (this.reviewCount == 1) {
            this.averageRating = 0.0;
            this.reviewCount = 0;
        } else {
            double totalRating = (this.averageRating * this.reviewCount) - rating;
            this.reviewCount--;
            this.averageRating = totalRating / this.reviewCount;
        }
    }

    public void updateReviewRating(Double oldRating, Double newRating) {
        if (this.reviewCount <= 0) return;

        double totalRating = (this.averageRating * this.reviewCount) - oldRating + newRating;
        this.averageRating = totalRating / this.reviewCount;
    }
}
