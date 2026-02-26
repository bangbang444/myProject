package bangbang.gourmet.social.repository;

import bangbang.gourmet.social.entity.Follow;
import bangbang.gourmet.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    // 팔로우 여부 체크
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 언팔로우
    void deleteByFollowerAndFollowing(User follower, User following);

    // 팔로우 수
    long countFollowersByFollowingId(Long followingId);
    // 팔로워 수
    long countFollowingsByFollowerId(Long followerId);

    @Query("select f.following.id from Follow f where f.follower.id = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);

    // 특정 유저의 팔로워 목록 (나를 팔로우하는 사람들)
    @Query("select f.follower from Follow f where f.following.id = :userId")
    List<User> findFollowersByFollowingId(@Param("userId") Long userId);

    // 특정 유저의 팔로잉 목록 (내가 팔로우하는 사람들)
    @Query("select f.following from Follow f where f.follower.id = :userId")
    List<User> findFollowingsByFollowerId(@Param("userId") Long userId);
}
