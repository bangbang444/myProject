package bangbang.gourmet.social.service;

import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.social.dto.FollowUserResponse;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private User requester;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        requester = User.builder().nickname("요청자").email("req@test.com").providerId("req-id").build();
        ReflectionTestUtils.setField(requester, "id", 1L);

        userA = User.builder().nickname("유저A").email("a@test.com").providerId("a-id").build();
        ReflectionTestUtils.setField(userA, "id", 2L);

        userB = User.builder().nickname("유저B").email("b@test.com").providerId("b-id").build();
        ReflectionTestUtils.setField(userB, "id", 3L);
    }

    // ─── getFollowers ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("팔로워 목록 조회 시 팔로워 유저 목록과 isFollowing 여부가 반환된다")
    void getFollowers_Success() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(userA));
        given(followRepository.findFollowingIdsByFollowerId(1L)).willReturn(List.of(2L)); // requester는 userA를 팔로우
        given(followRepository.findFollowersByFollowingId(2L)).willReturn(List.of(requester, userB));

        // when
        List<FollowUserResponse> result = followService.getFollowers(1L, 2L);

        // then
        assertThat(result).hasSize(2);

        FollowUserResponse requesterResponse = result.get(0);
        assertThat(requesterResponse.userId()).isEqualTo(1L);
        assertThat(requesterResponse.nickname()).isEqualTo("요청자");
        assertThat(requesterResponse.isFollowing()).isFalse(); // 자기 자신은 팔로우 중이 아님

        FollowUserResponse userBResponse = result.get(1);
        assertThat(userBResponse.userId()).isEqualTo(3L);
        assertThat(userBResponse.nickname()).isEqualTo("유저B");
        assertThat(userBResponse.isFollowing()).isFalse(); // userB는 팔로우 안 함
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 내가 팔로우 중인 유저는 isFollowing이 true다")
    void getFollowers_IsFollowingTrue() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(userA));
        given(followRepository.findFollowingIdsByFollowerId(1L)).willReturn(List.of(3L)); // requester는 userB를 팔로우
        given(followRepository.findFollowersByFollowingId(2L)).willReturn(List.of(userB));

        // when
        List<FollowUserResponse> result = followService.getFollowers(1L, 2L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(3L);
        assertThat(result.get(0).isFollowing()).isTrue();
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 팔로워가 없으면 빈 목록이 반환된다")
    void getFollowers_Empty() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(userA));
        given(followRepository.findFollowingIdsByFollowerId(1L)).willReturn(List.of());
        given(followRepository.findFollowersByFollowingId(2L)).willReturn(List.of());

        // when
        List<FollowUserResponse> result = followService.getFollowers(1L, 2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팔로워 목록 조회 시 대상 유저가 존재하지 않으면 NotFoundException이 발생한다")
    void getFollowers_TargetNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.getFollowers(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ─── getFollowings ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("팔로잉 목록 조회 시 팔로잉 유저 목록과 isFollowing 여부가 반환된다")
    void getFollowings_Success() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(userA));
        given(followRepository.findFollowingIdsByFollowerId(1L)).willReturn(List.of(3L)); // requester는 userB를 팔로우
        given(followRepository.findFollowingsByFollowerId(2L)).willReturn(List.of(requester, userB));

        // when
        List<FollowUserResponse> result = followService.getFollowings(1L, 2L);

        // then
        assertThat(result).hasSize(2);

        FollowUserResponse requesterResponse = result.get(0);
        assertThat(requesterResponse.userId()).isEqualTo(1L);
        assertThat(requesterResponse.isFollowing()).isFalse();

        FollowUserResponse userBResponse = result.get(1);
        assertThat(userBResponse.userId()).isEqualTo(3L);
        assertThat(userBResponse.isFollowing()).isTrue();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 팔로잉이 없으면 빈 목록이 반환된다")
    void getFollowings_Empty() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(userA));
        given(followRepository.findFollowingIdsByFollowerId(1L)).willReturn(List.of());
        given(followRepository.findFollowingsByFollowerId(2L)).willReturn(List.of());

        // when
        List<FollowUserResponse> result = followService.getFollowings(1L, 2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 시 대상 유저가 존재하지 않으면 NotFoundException이 발생한다")
    void getFollowings_TargetNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.getFollowings(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }
}
