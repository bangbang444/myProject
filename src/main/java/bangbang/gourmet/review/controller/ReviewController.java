package bangbang.gourmet.review.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewResponse;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import bangbang.gourmet.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/restaurant/{restaurantId}")
    public Response<Object> createReview(
            @PathVariable Long restaurantId,
            @RequestPart("request") @Valid ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @UserId Long userId
    ){
        return Response.success(SuccessCode.SUCCESS, reviewService.createReview(restaurantId, userId, request, images));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public Response<List<ReviewResponse>> getReviews(@PathVariable Long restaurantId) {
        return Response.success(SuccessCode.SUCCESS, reviewService.getReviews(restaurantId));
    }

    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<Void> updateReview(
            @UserId Long userId,
            @PathVariable Long reviewId,
            @RequestPart(value = "request") @Valid ReviewUpdateRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        reviewService.updateReview(userId, reviewId, request, newImages);

        return Response.success(SuccessCode.SUCCESS, null);
    }

    @DeleteMapping("/{reviewId}")
    public Response<Void> deleteReview(
            @UserId Long userId,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return Response.success(SuccessCode.SUCCESS, null);
    }
}
