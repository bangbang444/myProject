package bangbang.gourmet.user.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.user.controller.dto.FcmTokenRequest;
import bangbang.gourmet.user.service.UserFcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/fcm-token")
@RequiredArgsConstructor
public class UserFcmController {

    private final UserFcmService userFcmService;

    @PostMapping
    public Response<Void> saveFcmToken(
            @UserId Long userId,
            @RequestBody FcmTokenRequest request
    ) {
        userFcmService.saveFcmToken(userId, request.fcmToken());
        return Response.success(SuccessCode.SUCCESS, null);
    }

    @DeleteMapping
    public Response<Void> deleteFcmToken(@UserId Long userId) {
        userFcmService.deleteFcmToken(userId);
        return Response.success(SuccessCode.SUCCESS, null);
    }
}
