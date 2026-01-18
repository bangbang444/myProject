package bangbang.gourmet.user.service;

import bangbang.gourmet.config.KakaoProperties;
import bangbang.gourmet.user.controller.dto.KakaoTokenResponse;
import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static bangbang.gourmet.common.domain.Role.*;
import static bangbang.gourmet.common.domain.SocialProvider.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final WebClient kakaoAuthClient;
    private final WebClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;
    private final UserRepository userRepository;

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

    public KakaoUserInfo getKakaoUser(String accessToken) {
        return kakaoApiClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfo.class)
                .block();
    }

    public LoginResponse loginOrSignup(KakaoUserInfo userInfo){
        User user = userRepository.findByEmailAndProvider(userInfo.getEmail(), KAKAO)
                .orElseGet(() -> {
                    String tempNickname = UUID.randomUUID().toString().substring(0, 8);
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .nickname(tempNickname)
                            .role(ROLE_USER)
                            .providerId(String.valueOf(userInfo.id()))
                            .provider(KAKAO)
                            .build();
                    return userRepository.save(newUser);
                });

        // TODO: JWT 토큰 생성 로직
        return LoginResponse.of("token");
    }
}
