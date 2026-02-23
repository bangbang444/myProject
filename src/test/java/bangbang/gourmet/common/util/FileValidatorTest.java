package bangbang.gourmet.common.util;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {
    @Test
    @DisplayName("올바른 이미지 확장자(jpg, png 등)는 검증을 통과한다")
    void validateImageFile_Success() {
        // given
        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // when & then
        assertDoesNotThrow(() -> FileValidator.validateImageFile(validFile));
    }

    @Test
    @DisplayName("이미지 형식이 아닌 Content-Type은 예외를 던진다")
    void validateImageFile_InvalidContentType() {
        // given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello world".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> FileValidator.validateImageFile(textFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_FORMAT.getMessage());
    }

    @Test
    @DisplayName("확장자가 없는 파일은 예외를 던진다")
    void validateImageFile_NoExtension() {
        // given
        MockMultipartFile noExtFile = new MockMultipartFile(
                "file",
                "filename-without-extension",
                "image/jpeg",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> FileValidator.validateImageFile(noExtFile))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("허용되지 않은 확장자(exe, pdf 등)는 예외를 던진다")
    void validateImageFile_ForbiddenExtension() {
        // given
        MockMultipartFile forbiddenFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "image/jpeg", // Content-Type은 속였지만 확장자가 exe인 경우
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> FileValidator.validateImageFile(forbiddenFile))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("파일이 비어있거나(null) 없는 경우 검증을 그냥 통과한다")
    void validateImageFile_EmptyFile() {
        // given & when & then
        assertDoesNotThrow(() -> FileValidator.validateImageFile(null));

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "", new byte[0]);
        assertDoesNotThrow(() -> FileValidator.validateImageFile(emptyFile));
    }
}