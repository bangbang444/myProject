package bangbang.gourmet.user.service;

import bangbang.gourmet.common.security.jwt.service.RefreshTokenService;
import bangbang.gourmet.user.client.KakaoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @InjectMocks
    private UserAuthService userAuthService;

    @Mock private KakaoClient kakaoClient;
    @Mock private UserService userService;
    @Mock private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("로그아웃 시 Redis 리프레시 토큰이 삭제된다")
    void logout_DeletesRefreshToken() {
        Long userId = 1L;

        userAuthService.logout(userId);

        verify(refreshTokenService, times(1)).deleteRefreshToken(userId);
    }
}