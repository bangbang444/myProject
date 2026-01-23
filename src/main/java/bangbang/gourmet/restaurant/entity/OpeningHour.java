package bangbang.gourmet.restaurant.entity;

import bangbang.gourmet.crawler.RestaurantCrawledDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 식당의 시간인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String breakTime;
    private String lastOrder;

    public static OpeningHour of(RestaurantCrawledDto.OpeningHourDto dto, Restaurant restaurant) {
        return OpeningHour.builder()
                .restaurant(restaurant)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .breakTime(dto.getBreakTime())
                .lastOrder(dto.getLastOrder())
                .build();
    }
}
