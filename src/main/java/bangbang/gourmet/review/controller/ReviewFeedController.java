package bangbang.gourmet.review.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.FeedDetailResponse;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.service.ReviewFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewFeedController {

    private final ReviewFeedService reviewFeedService;

    @GetMapping("/feed")
    public Response<List<ReviewFeedResponse>> getReviewFeed(@UserId Long userId) {
        List<ReviewFeedResponse> feed = reviewFeedService.getFollowerFeed(userId);
        return Response.success(SuccessCode.SUCCESS, feed);
    }

    @GetMapping("/feed/{feedId}")
    public Response<FeedDetailResponse> getFeedDetail(
            @PathVariable Long feedId,
            @UserId Long userId) {
        return Response.success(SuccessCode.SUCCESS, reviewFeedService.getFeedDetail(feedId, userId));
    }
}
