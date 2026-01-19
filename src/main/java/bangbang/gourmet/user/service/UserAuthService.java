package bangbang.gourmet.user.service;

import bangbang.gourmet.user.client.KakaoClient;
import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final KakaoClient kakaoClient;
    private final UserService userService;

    public LoginResponse loginWithKakao(String code) {
        KakaoUserInfo kakaoUserInfo = kakaoClient.getUserInfo(code);
        return userService.loginOrRegister(kakaoUserInfo);
    }
}
