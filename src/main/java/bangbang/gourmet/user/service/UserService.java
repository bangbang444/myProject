package bangbang.gourmet.user.service;

import bangbang.gourmet.common.security.jwt.JwtTokenProvider;
import bangbang.gourmet.common.security.jwt.dto.TokenPair;
import bangbang.gourmet.user.controller.dto.KakaoUserInfo;
import bangbang.gourmet.user.controller.dto.LoginResponse;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static bangbang.gourmet.common.domain.Role.*;
import static bangbang.gourmet.common.domain.SocialProvider.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse loginOrRegister(KakaoUserInfo userInfo){
        User user = userRepository.findByEmailAndProvider(userInfo.getEmail(), KAKAO)
                .orElseGet(() -> {
                    String nickname = generateUniqueNickname(userInfo.getNickname());
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .nickname(nickname)
                            .role(ROLE_USER)
                            .providerId(String.valueOf(userInfo.id()))
                            .provider(KAKAO)
                            .build();
                    return userRepository.save(newUser);
                });

        TokenPair tokenPair = tokenProvider.generateTokenPair(user.getId(), user.getEmail());

        return LoginResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    private String generateUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        Random random = new Random();

        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + random.nextInt(10000);
        }
        return nickname;
    }
}
