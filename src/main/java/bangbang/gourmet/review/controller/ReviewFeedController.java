package bangbang.gourmet.review.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.service.ReviewFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews/feed")
public class ReviewFeedController {

    private final ReviewFeedService reviewFeedService;

    @GetMapping
    public ResponseEntity<List<ReviewFeedResponse>> getReviewFeed(@UserId Long userId) {
        List<ReviewFeedResponse> feed = reviewFeedService.getFollowerFeed(userId);
        return ResponseEntity.ok(feed);
    }
}
