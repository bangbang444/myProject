package bangbang.gourmet.social.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.notification.entity.NotificationType;
import bangbang.gourmet.notification.service.NotificationService;
import bangbang.gourmet.social.dto.FollowStatusResponse;
import bangbang.gourmet.social.dto.FollowUserResponse;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowInserter followInserter;
    private final NotificationService notificationService;

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
            boolean inserted = followInserter.tryInsert(follower, following);
            if (inserted) {
                notificationService.notify(following, follower, NotificationType.FOLLOW, null);
            }
            return new FollowStatusResponse(inserted);
        }
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowers(Long requesterId, Long targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Set<Long> myFollowingIds = new HashSet<>(followRepository.findFollowingIdsByFollowerId(requesterId));
        List<User> followers = followRepository.findFollowersByFollowingId(targetUserId);

        return followers.stream()
                .map(user -> FollowUserResponse.of(user, myFollowingIds.contains(user.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowings(Long requesterId, Long targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Set<Long> myFollowingIds = new HashSet<>(followRepository.findFollowingIdsByFollowerId(requesterId));
        List<User> followings = followRepository.findFollowingsByFollowerId(targetUserId);

        return followings.stream()
                .map(user -> FollowUserResponse.of(user, myFollowingIds.contains(user.getId())))
                .toList();
    }
}
