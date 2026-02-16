package bangbang.gourmet.social.repository;

import bangbang.gourmet.social.entity.Follow;
import bangbang.gourmet.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    // 팔로우 여부 체크
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 언팔로우
    void deleteByFollowerAndFollowing(User follower, User following);
}
