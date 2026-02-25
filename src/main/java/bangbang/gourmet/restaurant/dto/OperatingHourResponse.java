package bangbang.gourmet.restaurant.dto;

public record OperatingHourResponse(
        String dayOfWeek,   // "Mon", "Tue" 또는 "월", "화"
        String openingHours, // "11:30 ~ 22:00"
        String breakTime,    // "15:00 - 17:00"
        String lastOrder
) {
}
