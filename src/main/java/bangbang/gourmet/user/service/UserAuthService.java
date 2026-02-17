package bangbang.gourmet.user.service;

import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.user.client.KakaoClient;
import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static bangbang.gourmet.common.domain.SocialProvider.*;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final KakaoClient kakaoClient;
    private final UserService userService;

    public LoginResponse loginWithKakao(LoginRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoClient.getUserInfo(request.code());
        return userService.socialLogin(kakaoUserInfo, KAKAO);
    }
}
