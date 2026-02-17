package bangbang.gourmet.social.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
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
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowStatusResponse toggleFollow(Long followerId, Long followingId){
        // 자기 자신 팔로우 방지
        if(followerId.equals(followingId)){
            throw new BadRequestException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 유저 존재 확인
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

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
