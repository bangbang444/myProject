package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class UnauthorizedException extends GourmetException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
