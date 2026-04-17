package bangbang.gourmet.review.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.CursorPageResponse;
import bangbang.gourmet.review.dto.FeedDetailResponse;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.service.ReviewFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewFeedController {

    private final ReviewFeedService reviewFeedService;

    @GetMapping("/feed")
    public Response<CursorPageResponse<ReviewFeedResponse>> getReviewFeed(
            @UserId Long userId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size) {
        return Response.success(SuccessCode.SUCCESS, reviewFeedService.getFollowerFeed(userId, cursorId, size));
    }

    @GetMapping("/me")
    public Response<CursorPageResponse<ReviewFeedResponse>> getMyReviews(
            @UserId Long userId,
            @RequestParam boolean isPublic,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size) {
        return Response.success(SuccessCode.SUCCESS, reviewFeedService.getMyReviews(userId, isPublic, cursorId, size));
    }

    @GetMapping("/feed/{feedId}")
    public Response<FeedDetailResponse> getFeedDetail(
            @PathVariable Long feedId,
            @UserId Long userId) {
        return Response.success(SuccessCode.SUCCESS, reviewFeedService.getFeedDetail(feedId, userId));
    }
}
