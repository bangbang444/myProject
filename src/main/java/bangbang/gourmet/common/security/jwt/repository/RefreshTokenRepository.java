package bangbang.gourmet.common.security.jwt.repository;

import bangbang.gourmet.common.security.jwt.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * RefreshToken Redis Repository
 * CrudRepository를 상속하여 기본 CRUD 메서드 제공
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 조회
     * @Indexed 어노테이션으로 토큰 값에 인덱스가 설정되어 있어 빠른 조회 가능
     * @param token 리프레시 토큰 값
     * @return RefreshToken 엔티티
     */
    Optional<RefreshToken> findByToken(String token);
}