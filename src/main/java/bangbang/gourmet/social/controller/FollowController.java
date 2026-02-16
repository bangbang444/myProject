package bangbang.gourmet.social.controller;

import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.social.dto.FollowStatusResponse;
import bangbang.gourmet.social.service.FollowService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FollowController {
    private FollowService followService;

    @PostMapping("/follow/{followId}")
    public Response<FollowStatusResponse> follow(@PathVariable Long followId){
        return Response.success(SuccessCode.SUCCESS, followService.toggleFollow(followId, 1L));
    }
}
