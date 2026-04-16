package bangbang.gourmet.loadtest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Profile("loadtest")
public class LoadTestLoginInterceptor implements HandlerInterceptor {

    private static final long DEFAULT_USER_ID = 1L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader("X-User-Id");
        long userId = (userIdHeader != null) ? Long.parseLong(userIdHeader) : DEFAULT_USER_ID;
        request.setAttribute("userId", userId);
        return true;
    }
}
