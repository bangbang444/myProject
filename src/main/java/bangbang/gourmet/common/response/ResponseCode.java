package bangbang.gourmet.common.response;

import org.springframework.http.HttpStatus;

public interface ResponseCode {
    HttpStatus getCode();
    String getMessage();
}
