package bangbang.gourmet.user.service;

import bangbang.gourmet.user.controller.dto.KakaoTokenResponse;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.client.secret}")
    private String clientSecret;

    @Value("${kakao.client.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponse getKakaoToken(String code){

        WebClient webClient = WebClient.builder()
                .baseUrl("https://kauth.kakao.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        // TODO: 동기 - 비동기
        return webClient.post()
            .uri("/oauth/token")
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId) // REST API 키
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", redirectUri)
                    .with("code", code))
            .retrieve()
            .bodyToMono(KakaoTokenResponse.class)
            .block();
    }
}
