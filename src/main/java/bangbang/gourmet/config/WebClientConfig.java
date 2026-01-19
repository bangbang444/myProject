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


}
