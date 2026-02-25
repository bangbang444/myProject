package bangbang.gourmet.restaurant.controller;

import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.restaurant.dto.RestaurantDetailResponse;
import bangbang.gourmet.restaurant.dto.RestaurantResponse;
import bangbang.gourmet.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurant")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/nearby")
    public Response<List<RestaurantResponse>> getNearbyGems(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        List<RestaurantResponse> responses = restaurantService.getNearbyGems(lat, lon, size);
        return Response.success(SuccessCode.SUCCESS, responses);
    }

    @GetMapping("/{id}")
    public Response<RestaurantDetailResponse> getRestaurantDetail(@PathVariable Long id) {
        RestaurantDetailResponse response = restaurantService.getRestaurantDetail(id);

        return Response.success(SuccessCode.SUCCESS, response);
    }
}
