package bangbang.gourmet.social.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.social.dto.ReviewLikeResponse;
import bangbang.gourmet.social.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewLikeController {
    private final ReviewLikeService reviewLikeService;

    @PostMapping("/{reviewId}/like")
    public Response<ReviewLikeResponse> toggleLike(
            @PathVariable Long reviewId,
            @UserId Long userId
    ) {
        return Response.success(SuccessCode.SUCCESS, reviewLikeService.toggleLike(userId, reviewId));
    }
}
