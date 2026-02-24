package bangbang.gourmet.restaurant.controller;

import bangbang.gourmet.restaurant.dto.RestaurantResponse;
import bangbang.gourmet.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurant")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantResponse>> getNearbyGems(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        List<RestaurantResponse> responses = restaurantService.getNearbyGems(lat, lon, size);
        return ResponseEntity.ok(responses);
    }
}
