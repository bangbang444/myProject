package bangbang.gourmet.restaurant.controller;

import bangbang.gourmet.restaurant.service.RestaurantDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestaurantDataController {
    private final RestaurantDataService restaurantDataService;

    //@GetMapping("/update-categories")
    public String updateCategories() {
        restaurantDataService.updateMissingCategories();
        return "데이터 업데이트 프로세스가 시작되었습니다. 서버 로그를 확인하세요!";
    }
}
