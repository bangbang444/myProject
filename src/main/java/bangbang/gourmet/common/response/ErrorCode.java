package bangbang.gourmet.common.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode{
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");


    private final HttpStatus code;
    private final String message;

    @Override
    public HttpStatus getCode(){
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
