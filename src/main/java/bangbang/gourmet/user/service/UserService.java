package bangbang.gourmet.user.service;

import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static bangbang.gourmet.common.domain.Role.*;
import static bangbang.gourmet.common.domain.SocialProvider.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public LoginResponse loginOrRegister(KakaoUserInfo userInfo){
        User user = userRepository.findByEmailAndProvider(userInfo.getEmail(), KAKAO)
                .orElseGet(() -> {
                    String tempNickname = UUID.randomUUID().toString().substring(0, 8);
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .nickname(tempNickname)
                            .role(ROLE_USER)
                            .providerId(String.valueOf(userInfo.id()))
                            .provider(KAKAO)
                            .build();
                    return userRepository.save(newUser);
                });

        // TODO: JWT 토큰 생성 로직
        return LoginResponse.of("token");
    }
}
