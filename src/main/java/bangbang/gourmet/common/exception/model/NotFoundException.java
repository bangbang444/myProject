package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class NotFoundException extends GourmetException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
