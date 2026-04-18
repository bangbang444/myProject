package bangbang.gourmet.notification.controller;

import bangbang.gourmet.common.annotation.UserId;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.common.response.SuccessCode;
import bangbang.gourmet.notification.dto.NotificationResponse;
import bangbang.gourmet.notification.service.NotificationQueryService;
import bangbang.gourmet.review.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public Response<CursorPageResponse<NotificationResponse>> getNotifications(
            @UserId Long userId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size
    ) {
        return Response.success(SuccessCode.SUCCESS, notificationQueryService.getNotifications(userId, cursorId, size));
    }

    @GetMapping("/unread-count")
    public Response<Long> getUnreadCount(@UserId Long userId) {
        return Response.success(SuccessCode.SUCCESS, notificationQueryService.getUnreadCount(userId));
    }

}
