package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class GourmetException extends RuntimeException {

    private final ErrorCode errorCode;

    public GourmetException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
