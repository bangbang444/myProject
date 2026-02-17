package bangbang.gourmet.social.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.social.dto.FollowStatusResponse;
import bangbang.gourmet.social.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping("/follow/{followId}")
    public Response<FollowStatusResponse> follow(@UserId Long userId, @PathVariable Long followId){
        return Response.success(SuccessCode.SUCCESS, followService.toggleFollow(userId, followId));
    }
}
