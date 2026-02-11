package bangbang.gourmet.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationService {
    @Value("${logging.discord.error.400}")
    private String webhook4xxUrl;

    @Value("${logging.discord.error.500}")
    private String webhook5xxUrl;

    private final WebClient discordClient;

    @Async("discordExecutor")
    public void send5xxNotification(String errorMessage, String stackTrace, String requestInfo){
        try{
            String message = create5xxMessage(errorMessage, stackTrace, requestInfo);
            sendToDiscordAsync(message, webhook5xxUrl, "🚨 서버 에러 발생 🚨");
        }catch (Exception e){
            log.error("Discord 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    @Async("discordExecutor")
    public void send4xxNotification(String errorMessage, String requestInfo, String clientInfo) {
        try {
            if (webhook4xxUrl == null || webhook4xxUrl.isEmpty()) {
                log.debug("400대 에러 웹훅 URL이 설정되지 않음");
                return;
            }

            String message = create4xxMessage(errorMessage, requestInfo, clientInfo);
            sendToDiscordAsync(message, webhook4xxUrl, "👻 클라이언트 에러 발생 👻");
        } catch (Exception e) {
            log.error("Discord 4xx 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    private String create4xxMessage(String errorMessage, String requestInfo, String clientInfo) {
        StringBuilder message = new StringBuilder();
        message.append("**에러 발생 시간:** ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n");

        message.append("**에러 메시지:** ")
                .append(nz(errorMessage, "Unknown Error"))
                .append("\n");

        if (nz(requestInfo, "").contains(" ")) {
            String[] parts = requestInfo.split(" ", 2);
            if (parts.length >= 2) {
                String method = parts[0];
                String uri = parts[1];
                message.append("**요청 메소드:** ").append(method).append("\n");
                message.append("**요청 URI:** ").append(uri).append("\n");
            }
        }

        if (clientInfo != null && !clientInfo.isEmpty()) {
            message.append("**클라이언트 정보:** ")
                    .append(clientInfo)
                    .append("\n");
        }
        message.append("========================================");

        return message.toString();
    }

    private String create5xxMessage(String errorMessage, String stackTrace, String requestInfo) {
        StringBuilder message = new StringBuilder();
        message.append("**에러 발생 시간:** ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n");

        message.append("**에러 메시지:** ")
                .append(nz(errorMessage, "Unknown Error"))
                .append("\n");

        if (nz(requestInfo, "").contains(" ")) {
            String[] parts = requestInfo.split(" ", 2); // 최대 2개로 나눔
            if (parts.length >= 2) {
                String method = parts[0];
                String uri = parts[1];
                message.append("**요청 메소드:** ").append(method).append("\n");
                message.append("**요청 URI:** ").append(uri);
            }
        }

        message.append("\n").append("========================================");
        message.append("```\n");
        String st = (stackTrace == null) ? "No stack trace available"
                : (stackTrace.length() > 1800 ? stackTrace.substring(0, 1800) + "..." : stackTrace);
        message.append(st).append("\n```");

        return message.toString();
    }

    private void sendToDiscordAsync(String message, String webhookUrl, String title) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("content", message);
        body.put("username", title);

        discordClient.post()
                .uri(webhookUrl)
                .bodyValue(body) // Map 넣으면 자동 Json
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Discord 웹훅 전송 실패: {}", e.getMessage()))
                .subscribe(); // 비동기 실행 (first-and-forget)
    }

    private static String nz(String v, String d) { return (v == null || v.isBlank()) ? d : v; }
}
