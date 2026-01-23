package bangbang.gourmet.crawler;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class RestaurantCrawledDto {
    private String name;
    private List<String> categories;
    private String address;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    private List<OpeningHourDto> openingHours;
    private List<MenuDto> menus;



    @Getter @Builder
    public static class OpeningHourDto {
        private String dayOfWeek;
        private String startTime;
        private String endTime;
        private String breakTime;
        private String lastOrder;
    }

    @Getter @Builder
    public static class MenuDto {
        private String name;
        private String price;
    }
}
