package bangbang.gourmet.user.client;

import bangbang.gourmet.user.controller.dto.SocialUserInfo;

public interface SocialClient {
    SocialUserInfo getUserInfo(String code);
}
