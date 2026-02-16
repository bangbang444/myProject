package bangbang.gourmet.social.service;

import bangbang.gourmet.social.dto.FollowStatusResponse;
import bangbang.gourmet.social.entity.Follow;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowStatusResponse toggleFollow(Long followerId, Long followingId){
        // 자기 자신 팔로우 방지
        if(followerId.equals(followingId)){

        };

        // 유저 존재 확인
        User follower = userRepository.findById(followerId)
                .orElseThrow();
        User following = userRepository.findById(followingId)
                .orElseThrow();

        // 토글
        if(followRepository.existsByFollowerAndFollowing(follower, following)){
            followRepository.deleteByFollowerAndFollowing(follower, following);
            return new FollowStatusResponse(false);
        }else{
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            followRepository.save(follow);
            return new FollowStatusResponse(true);
        }

    }
}
