package bangbang.gourmet.common.interceptor;

import bangbang.gourmet.common.exception.model.UnauthorizedException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtTokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractToken(request);
        if(token == null){
            throw new UnauthorizedException(ErrorCode.TOKEN_NOT_FOUND);
        }

        if(!tokenProvider.validateToken(token)){
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }

        try{
            Long userId = Long.parseLong(tokenProvider.getSubject(token));
            request.setAttribute("userId", userId);
        }catch (NumberFormatException e){
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN);
        }
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
