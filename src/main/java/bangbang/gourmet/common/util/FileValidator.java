package bangbang.gourmet.common.util;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.response.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidator {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");

    public static void validateImageFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            return; // 파일이 없는 경우 검증 통과 (서비스 로직에서 처리)
        }

        // 1. Content-Type 확인
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // 2. 확장자 추출 및 검사
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_FORMAT);
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // 확장자와 Content-Type 매칭 검증
        if (("png".equals(extension) && !"image/png".equals(contentType)) ||
                (("jpg".equals(extension) || "jpeg".equals(extension)) && !"image/jpeg".equals(contentType))) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }
}
