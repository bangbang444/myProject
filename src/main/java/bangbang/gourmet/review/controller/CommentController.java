package bangbang.gourmet.review.controller;


import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.CommentCreateRequest;
import bangbang.gourmet.review.dto.CommentCreateResponse;
import bangbang.gourmet.review.dto.CommentResponse;
import bangbang.gourmet.review.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class CommentController {
    private final CommentService commentService;

    // 1. 댓글 작성
    @PostMapping("/{reviewId}/comments")
    public Response<CommentCreateResponse> createComment(
            @PathVariable Long reviewId,
            @UserId Long userId,
            @RequestBody CommentCreateRequest request
    ) {;
        return Response.success(SuccessCode.SUCCESS, commentService.createComment(userId, reviewId, request.content()));
    }

    // 2. 댓글 목록 조회
    @GetMapping("/{reviewId}/comments")
    public Response<List<CommentResponse>> getComments(
            @PathVariable Long reviewId,
            @UserId Long userId
    ) {
        List<CommentResponse> comments = commentService.getComments(reviewId, userId);
        return Response.success(SuccessCode.SUCCESS, comments);
    }
}
