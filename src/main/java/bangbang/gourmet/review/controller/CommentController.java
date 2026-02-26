package bangbang.gourmet.review.controller;


import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.review.dto.CommentCreateRequest;
import bangbang.gourmet.review.dto.CommentCreateResponse;
import bangbang.gourmet.review.dto.CommentResponse;
import bangbang.gourmet.review.dto.CommentUpdateRequest;
import bangbang.gourmet.review.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
    @RequestMapping("/api/comments")
    public class CommentController {
        private final CommentService commentService;

        // 1. 댓글 작성
        @PostMapping("/reviews/{reviewId}")
        public Response<CommentCreateResponse> createComment(
                @PathVariable Long reviewId,
                @UserId Long userId,
                @RequestBody CommentCreateRequest request
        ) {
            return Response.success(SuccessCode.SUCCESS, commentService.createComment(userId, reviewId, request.content()));
        }

    // 2. 댓글 목록 조회
    @GetMapping("/reviews/{reviewId}")
    public Response<List<CommentResponse>> getComments(
            @PathVariable Long reviewId,
            @UserId Long userId
    ) {
        List<CommentResponse> comments = commentService.getComments(reviewId, userId);
        return Response.success(SuccessCode.SUCCESS, comments);
    }

    @PatchMapping("/{commentId}")
    public Response<Void> updateComment(
            @UserId Long userId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        commentService.updateComment(userId, commentId, request);

        return Response.success(SuccessCode.SUCCESS, null);
    }

    @DeleteMapping("/{commentId}")
    public Response<Void> deleteComment(
            @UserId Long userId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userId, commentId);
        return Response.success(SuccessCode.SUCCESS, null);
    }
}
