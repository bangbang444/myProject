package bangbang.gourmet.social.service;

import bangbang.gourmet.social.entity.Follow;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowInserter {

    private final FollowRepository followRepository;

    public boolean tryInsert(User follower, User following) {
        try {
            followRepository.save(Follow.builder().follower(follower).following(following).build());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}