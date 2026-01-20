package bangbang.gourmet.user.client;

import bangbang.gourmet.config.KakaoProperties;
import bangbang.gourmet.user.controller.dto.KakaoTokenResponse;
import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    private final WebClient kakaoAuthClient;
    private final WebClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;

    public KakaoUserInfo getUserInfo(String code){
        KakaoTokenResponse tokens = getKakaoToken(code);
        return getKakaoUserInfo(tokens.accessToken());
    }

    public KakaoTokenResponse getKakaoToken(String code){
        // TODO: 동기 - 비동기
        return kakaoAuthClient.post()
                .uri("/oauth/token")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", kakaoProperties.id()) // REST API 키
                        .with("client_secret", kakaoProperties.secret())
                        .with("redirect_uri", kakaoProperties.redirectUri())
                        .with("code", code))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        return kakaoApiClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfo.class)
                .block();
    }
}
