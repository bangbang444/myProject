package bangbang.gourmet.restaurant.dto;

import bangbang.gourmet.review.dto.ReviewSimpleResponse;

import java.util.List;

public record RestaurantDetailResponse(
        Long id,
        String name,
        String category,
        String areaName,
        String fullAddress,
        String phoneNumber,
        String description,
        String todayOpeningHours,
        String breakTime,
        String lastOrder,
        Boolean isOpen,
        List<OperatingHourResponse> operatingHours,
        Double rating,
        Integer reviewCount,
        List<ReviewSimpleResponse> recentReviews,
        List<String> imageUrls,
        List<MenuResponse> menuList
) {}
