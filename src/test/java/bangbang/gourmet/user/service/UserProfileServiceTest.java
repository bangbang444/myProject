package bangbang.gourmet.user.service;

import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.controller.dto.ProfileResponseDto;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private S3Service s3Service;

    private User requester;
    private User target;

    @BeforeEach
    void setUp() {
        requester = User.builder().nickname("요청자").email("req@test.com").providerId("req-id").build();
        ReflectionTestUtils.setField(requester, "id", 1L);

        target = User.builder().nickname("대상유저").email("target@test.com").providerId("target-id").build();
        ReflectionTestUtils.setField(target, "id", 2L);
    }

    @Test
    @DisplayName("내 프로필을 조회하면 닉네임, 팔로워/팔로잉 수가 포함된 DTO가 반환된다")
    void getProfile_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(followRepository.countFollowingsByFollowerId(1L)).willReturn(3L);
        given(followRepository.countFollowersByFollowingId(1L)).willReturn(7L);

        // when
        ProfileResponseDto result = userProfileService.getProfile(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("요청자");
        assertThat(result.followerCount()).isEqualTo(3L);
        assertThat(result.followingCount()).isEqualTo(7L);
        assertThat(result.isFollowing()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 userId로 내 프로필을 조회하면 NotFoundException이 발생한다")
    void getProfile_UserNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getProfile(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("팔로우 중인 유저의 프로필 조회 시 isFollowing이 true로 반환된다")
    void getUserProfile_WhenFollowing_ReturnsIsFollowingTrue() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(followRepository.countFollowingsByFollowerId(2L)).willReturn(5L);
        given(followRepository.countFollowersByFollowingId(2L)).willReturn(10L);
        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(followRepository.existsByFollowerAndFollowing(requester, target)).willReturn(true);

        // when
        ProfileResponseDto result = userProfileService.getUserProfile(1L, 2L);

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.nickname()).isEqualTo("대상유저");
        assertThat(result.followerCount()).isEqualTo(5L);
        assertThat(result.followingCount()).isEqualTo(10L);
        assertThat(result.isFollowing()).isTrue();
    }

    @Test
    @DisplayName("팔로우하지 않은 유저의 프로필 조회 시 isFollowing이 false로 반환된다")
    void getUserProfile_WhenNotFollowing_ReturnsIsFollowingFalse() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(followRepository.countFollowingsByFollowerId(2L)).willReturn(0L);
        given(followRepository.countFollowersByFollowingId(2L)).willReturn(0L);
        given(userRepository.findById(1L)).willReturn(Optional.of(requester));
        given(followRepository.existsByFollowerAndFollowing(requester, target)).willReturn(false);

        // when
        ProfileResponseDto result = userProfileService.getUserProfile(1L, 2L);

        // then
        assertThat(result.isFollowing()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 대상 유저 ID로 프로필 조회 시 NotFoundException이 발생한다")
    void getUserProfile_TargetNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getUserProfile(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 요청자 ID로 프로필 조회 시 NotFoundException이 발생한다")
    void getUserProfile_RequesterNotFound() {
        // given
        given(userRepository.findById(2L)).willReturn(Optional.of(target));
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getUserProfile(99L, 2L))
                .isInstanceOf(NotFoundException.class);
    }
}
