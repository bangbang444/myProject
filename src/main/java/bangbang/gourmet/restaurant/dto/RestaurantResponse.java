package bangbang.gourmet.restaurant.dto;

public record RestaurantResponse(
        Long id,
        String name,
        String category,    // 일식
        String areaName,    // 역삼
        Double rating,      // 4.8
        Double distance,    // 0.5 (단위: km)
        String thumbnailUrl // 이미지 URL
) {
    public RestaurantResponse {
        // 소수점 첫째 자리까지 반올림 처리 (예: 0.52 -> 0.5)
        if (distance != null) {
            distance = Math.round(distance * 10) / 10.0;
        }
    }
}
