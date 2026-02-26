package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.ProfileImageUpdateDto;
import bangbang.gourmet.user.controller.dto.ProfileResponseDto;
import bangbang.gourmet.user.controller.dto.ProfileUpdateDto;
import bangbang.gourmet.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 내 프로필 조회
    @GetMapping("/me")
    public Response<ProfileResponseDto> getProfile(@UserId Long userId){
        return Response.success(SuccessCode.SUCCESS, userProfileService.getProfile(userId));
    }

    // 다른 유저 프로필 조회
    @GetMapping("/{targetUserId}")
    public Response<ProfileResponseDto> getUserProfile(@UserId Long userId, @PathVariable Long targetUserId){
        return Response.success(SuccessCode.SUCCESS, userProfileService.getUserProfile(userId, targetUserId));
    }

    @PatchMapping("/me/info")
    public Response<Void> updateInfo(@UserId Long userId, @RequestBody @Valid ProfileUpdateDto dto){
        userProfileService.updateProfileInfo(userId, dto);
        return Response.success(SuccessCode.SUCCESS, null);
    }

    @PostMapping("/me/image")
    public Response<ProfileImageUpdateDto> updateImage(@UserId Long userId, @RequestParam("file") MultipartFile file){
        return Response.success(SuccessCode.SUCCESS, userProfileService.updateProfileImage(userId, file));
    }

}
