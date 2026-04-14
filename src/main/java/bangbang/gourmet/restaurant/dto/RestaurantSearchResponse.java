package bangbang.gourmet.restaurant.dto;

public record RestaurantSearchResponse(
        Long id,
        String name,
        String category,
        String areaName,
        String thumbnailUrl
) {
}
