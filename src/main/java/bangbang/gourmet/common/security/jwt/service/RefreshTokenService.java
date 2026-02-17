package bangbang.gourmet.common.security.jwt.service;

import bangbang.gourmet.common.security.jwt.entity.RefreshToken;
import bangbang.gourmet.common.security.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소 관리 서비스
 * Redis를 사용하여 리프레시 토큰을 저장, 조회, 삭제합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 리프레시 토큰 저장 (기존 토큰 있으면 덮어쓰기)
     * @param userId 사용자 ID (Redis Key)
     * @param refreshToken 리프레시 토큰 값
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        // log.info("리프레시 토큰 저장 시작: userId={});

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(token);
        log.info("리프레시 토큰 저장 완료: userId={}", userId);
    }

    /**
     * User ID로 토큰 조회
     * @param userId 사용자 ID
     * @return RefreshToken 엔티티
     */
    public Optional<RefreshToken> findByUserId(Long userId) {
        return refreshTokenRepository.findById(userId);
    }

    /**
     * 토큰 문자열로 조회 (향후 /refresh 엔드포인트용)
     * @param token 리프레시 토큰 값
     * @return RefreshToken 엔티티
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * 리프레시 토큰 삭제 (로그아웃 또는 재로그인 시)
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(Long userId) {
        log.info("리프레시 토큰 삭제: userId={}", userId);
        refreshTokenRepository.deleteById(userId);
    }

    /**
     * 토큰 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    public boolean existsByUserId(Long userId) {
        return refreshTokenRepository.existsById(userId);
    }
}