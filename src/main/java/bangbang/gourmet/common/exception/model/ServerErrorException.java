package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class ServerErrorException extends GourmetException {
    public ServerErrorException(ErrorCode errorCode) {
        super(errorCode);
    }
}
