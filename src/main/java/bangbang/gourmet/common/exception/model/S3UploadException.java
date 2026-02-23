package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class S3UploadException extends GourmetException {
    public S3UploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
