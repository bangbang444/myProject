package bangbang.gourmet.common.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode{
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 파일 형식입니다. 이미지 파일만 업로드 가능합니다."),
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 식당입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않은 리뷰입니다."),
    NOT_OWNER_ERROR(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정 또는 삭제할 수 있습니다."),
    INVALID_IMAGE_OWNER(HttpStatus.BAD_REQUEST, "해당 리뷰에 속하지 않은 이미지는 수정하거나 삭제할 수 없습니다.");

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
