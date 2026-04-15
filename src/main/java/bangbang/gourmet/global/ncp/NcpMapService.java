package bangbang.gourmet.global.ncp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcpMapService {

    private final WebClient ncpWebClient;

    @Value("${ncp.client-id}")
    private String clientId;

    @Value("${ncp.client-secret}")
    private String clientSecret;

    /**
     * 좌표로 행정구역 주소를 조회합니다.
     * 실패 시 null을 반환합니다.
     */
    public AddressResult reverseGeocode(Double lat, Double lng) {
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

        if (dto == null || dto.results() == null || dto.results().isEmpty()) {
            log.warn("NCP API 응답 결과가 없습니다. (lat={}, lng={})", lat, lng);
            return null;
        }

        var region = dto.results().get(0).region();
        return new AddressResult(
                getAreaName(region.area1()),
                getAreaName(region.area2()),
                getAreaName(region.area3())
        );
    }

    private String getAreaName(NaverMapDto.Area area) {
        if (area == null || area.name() == null || area.name().isBlank()) {
            return null;
        }
        return area.name();
    }
}
