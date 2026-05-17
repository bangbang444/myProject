package bangbang.gourmet.user.service;

import bangbang.gourmet.user.controller.dto.LoginRequest;
import bangbang.gourmet.user.controller.dto.LoginResponse;

public interface SocialAuthService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(Long userId);
}
