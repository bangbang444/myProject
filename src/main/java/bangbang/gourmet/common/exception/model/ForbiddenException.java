package bangbang.gourmet.common.exception.model;

import bangbang.gourmet.common.response.ErrorCode;

public class ForbiddenException extends GourmetException {
  public ForbiddenException(ErrorCode errorCode) {
    super(errorCode);
  }
}
