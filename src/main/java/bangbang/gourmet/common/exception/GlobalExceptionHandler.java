package bangbang.gourmet.common.exception;

import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.response.Response;
import bangbang.gourmet.discord.DiscordNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final DiscordNotificationService discordService;

    // 500 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handle500(Exception ex, HttpServletRequest request) {
        log.error("[500 에러 발생] 경로: {} {}, 메시지: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        String requestInfo = request.getMethod() + " " + request.getRequestURI();

        // 스택 트레이스 문자열 변환
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));

        // 디스코드 전송 (아까 만든 서비스 호출)
        discordService.send5xxNotification(ex.getMessage(), sw.toString(), requestInfo);

        return ResponseEntity.internalServerError().body(Response.error(ErrorCode.INTERNAL_SERVER_ERROR,"서버 내부 오류가 발생했습니다."));
    }

    // 올바르지 않은 경로(4xx 에러)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<Response<String>> handle400(Exception ex, HttpServletRequest request) {
        // 1. 요청 정보 (Method + URI)
        String requestInfo = request.getMethod() + " " + request.getRequestURI();

        // 2. 클라이언트 정보 (IP + User-Agent)
        String clientIP = getClientIP(request);
        String clientInfo = String.format("IP: %s | Agent: %s",
                clientIP,
                request.getHeader("User-Agent"));

        // 3. 4xx 알림 전송
        if(request.getRequestURI().startsWith("/api")){
            discordService.send4xxNotification("존재하지 않는 API 경로 요청", requestInfo, clientInfo);
        }

        return ResponseEntity.badRequest()
                .body(Response.error(ErrorCode.NOT_FOUND, ex.getMessage()));
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // 마지막 수단
        }
        return ip;
    }
}
