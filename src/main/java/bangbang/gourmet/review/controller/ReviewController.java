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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant/")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/{restaurantId}/reviews")
    public Response<Object> createReview(
            @PathVariable Long restaurantId,
            @RequestPart("request") @Valid ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @UserId Long userId
    ){
        return Response.success(SuccessCode.SUCCESS, reviewService.createReview(restaurantId, userId, request, images));
    }

    @GetMapping("/{restaurantId}/reviews")
    public Response<List<ReviewResponse>> getReviews(@PathVariable Long restaurantId) {
        return Response.success(SuccessCode.SUCCESS, reviewService.getReviews(restaurantId));
    }

    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<Void> updateReview(
            @UserId Long userId,
            @PathVariable Long reviewId,
            @RequestPart(value = "request") ReviewUpdateRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        // DTO에 newImages를 새로 담아서 서비스로 넘겨줍니다.
        // (DTO 구조에 따라 request.withNewImages(newImages) 처럼 처리하거나
        // 서비스 파라미터를 수정해서 넘길 수 있습니다.)
        reviewService.updateReview(userId, reviewId, request, newImages);

        return Response.success(SuccessCode.SUCCESS, null);
    }
}
