package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class BadRequestException extends GourmetException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
