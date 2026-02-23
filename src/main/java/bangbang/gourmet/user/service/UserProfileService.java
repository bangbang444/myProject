package bangbang.gourmet.user.service;

import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.global.s3.S3Buckets;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.user.controller.dto.ProfileImageUpdateDto;
import bangbang.gourmet.user.controller.dto.ProfileResponseDto;
import bangbang.gourmet.user.controller.dto.ProfileUpdateDto;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static bangbang.gourmet.global.s3.S3Buckets.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public ProfileResponseDto getProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return ProfileResponseDto.from(user, user.getProfileImageKey());
    }

    @Transactional
    public void updateProfileInfo(Long userId, ProfileUpdateDto dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(dto.nickname(), dto.bio());
    }

    @Transactional
    public ProfileImageUpdateDto updateProfileImage(Long userId, MultipartFile file){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        String finalKey = null;

        if(file != null && !file.isEmpty()) {
            // 이미지 교체
            if (user.getProfileImageKey() != null) {
                s3Service.delete(USER, user.getProfileImageKey());
            }

            finalKey = s3Service.upload(file, USER, "profiles");
            user.updateProfileImage(finalKey);
        }else{
            // 기본 이미지
            if (user.getProfileImageKey() != null) {
                s3Service.delete(USER, user.getProfileImageKey());
                user.updateProfileImage(null); // DB 필드 초기화
            }
            finalKey = null;
        }

        log.info("finalKey={}", finalKey);

        return ProfileImageUpdateDto.from(finalKey);
    }
}
