package bangbang.gourmet.review.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
}
