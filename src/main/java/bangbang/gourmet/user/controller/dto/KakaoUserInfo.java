package bangbang.gourmet.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount,
        @JsonProperty("properties") KakaoProfile properties
) {
    public record KakaoAccount(String email){}
    public record KakaoProfile(String nickname){}

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String getNickname() {
        return properties != null ? properties.nickname() : null;
    }
}
