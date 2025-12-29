package bangbang.gourmet.common.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {
    SUCCESS(HttpStatus.OK, "요청에 성공하였습니다.");

    private final HttpStatus code;
    private final String message;


    @Override
    public HttpStatus getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
