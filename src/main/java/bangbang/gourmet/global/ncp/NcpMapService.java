package bangbang.gourmet.global.ncp;

import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcpMapService {

    private final WebClient ncpWebClient;
    private final RestaurantRepository restaurantRepository;

    @Value("${ncp.client-id}")
    private String clientId;

    @Value("${ncp.client-secret}")
    private String clientSecret;

    public void reverseGeocode(Long restaurantId, Double lat, Double lng) {
        try {
            NaverMapDto dto = ncpWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/map-reversegeocode/v2/gc")
                            .queryParam("coords", lng + "," + lat) // NCP는 경도(x), 위도(y) 순서
                            .queryParam("output", "json")
                            .queryParam("orders", "addr,roadaddr")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverMapDto.class)
                    .block();

            if (dto != null) {
                processAddress(restaurantId, dto);
            }
        }catch(Exception e){
            // 예외 발생 시 전체 스택 트레이스를 로그에 남깁니다.
            log.error("식당 ID {} 주소 정제 중 오류 발생", restaurantId, e);
            throw e; // 트랜잭션 롤백이 필요하다면 예외를 다시 던집니다.
        }
    }

    private void processAddress(Long restaurantId, NaverMapDto dto) {
        if (dto.results() == null || dto.results().isEmpty()) {
            log.warn("NCP API 응답 결과가 없습니다.");
            return;
        }

        // 1. 첫 번째 결과(지번 주소)에서 지역 정보 추출
        var firstResult = dto.results().get(0);
        var region = firstResult.region();

        String sido = getAreaName(region.area1()); // area1: 서울특별시, 경기도
        String sigungu = getAreaName(region.area2());  // area2: 서초구, 수원시 팔달구
        String dong = getAreaName(region.area3());     // area3: 서초동, 인계동

        // 3. 로그 출력 (이후 여기서 Repository를 통해 DB Update 수행)
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            String fullAddress = String.format("%s (%s)", restaurant.getAddress(), restaurant.getRestaurantName());
            restaurant.updateAddress(sido, sigungu, dong, fullAddress);

            restaurantRepository.save(restaurant);
            //log.info("정제 완료 - 식당 ID: {}, 동: {}", restaurantId, dong);
        });
    }

    private String getAreaName(NaverMapDto.Area area) {
        if (area == null || area.name() == null || area.name().isBlank()) {
            return null;
        }
        return area.name();
    }
}
