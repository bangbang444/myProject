package bangbang.gourmet.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(String email){}
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email : null;
    }
}
