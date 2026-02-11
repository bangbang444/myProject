package bangbang.gourmet.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KakaoProperties kakaoProperties;

    @Bean
    public WebClient kakaoAuthClient(){
        return WebClient.builder()
                .baseUrl(kakaoProperties.authUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public WebClient kakaoApiClient() {
        return WebClient.builder()
                .baseUrl(kakaoProperties.apiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public WebClient discordClient() {
        return WebClient.builder()
                // 디스코드는 전송 시마다 URL이 달라질 수 있으므로 baseUrl은 비워두거나
                // 공통적인 API root가 있다면 넣어도 됩니다. (보통은 비워둡니다)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
