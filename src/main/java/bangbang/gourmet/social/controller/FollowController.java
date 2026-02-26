package bangbang.gourmet.social.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.social.dto.FollowStatusResponse;
import bangbang.gourmet.social.dto.FollowUserResponse;
import bangbang.gourmet.social.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping("/follow/{followId}")
    public Response<FollowStatusResponse> follow(@UserId Long userId, @PathVariable Long followId){
        return Response.success(SuccessCode.SUCCESS, followService.toggleFollow(userId, followId));
    }

    @GetMapping("/users/{targetUserId}/followers")
    public Response<List<FollowUserResponse>> getFollowers(@UserId Long userId, @PathVariable Long targetUserId){
        return Response.success(SuccessCode.SUCCESS, followService.getFollowers(userId, targetUserId));
    }

    @GetMapping("/users/{targetUserId}/followings")
    public Response<List<FollowUserResponse>> getFollowings(@UserId Long userId, @PathVariable Long targetUserId){
        return Response.success(SuccessCode.SUCCESS, followService.getFollowings(userId, targetUserId));
    }
}
