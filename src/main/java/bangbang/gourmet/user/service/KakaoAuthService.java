package bangbang.gourmet.user.service;

import bangbang.gourmet.common.security.jwt.service.RefreshTokenService;
import bangbang.gourmet.user.client.SocialClient;
import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static bangbang.gourmet.common.domain.SocialProvider.KAKAO;

@Service
@RequiredArgsConstructor
public class KakaoAuthService implements SocialAuthService {

    private final SocialClient socialClient;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        return userService.socialLogin(socialClient.getUserInfo(request.code()), KAKAO);
    }

    @Override
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshToken(userId);
    }
}
