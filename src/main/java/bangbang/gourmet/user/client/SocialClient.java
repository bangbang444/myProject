package bangbang.gourmet.user.client;

import bangbang.gourmet.user.controller.dto.KakaoUserInfo;

public interface SocialClient {
    KakaoUserInfo getUserInfo(String code);
}
