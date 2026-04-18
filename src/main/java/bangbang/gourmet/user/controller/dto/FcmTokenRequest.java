package bangbang.gourmet.user.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(@NotBlank String fcmToken) {
}
