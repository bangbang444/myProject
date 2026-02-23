package bangbang.gourmet.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@SpringBootTest
class S3ConfigTest {

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "user";
    private static final String TEST_KEY = "spring-bean-test.txt";

    @Test
    @DisplayName("버킷 내 모든 파일 목록 조회")
    void listObjectsTest() {
        ListObjectsV2Response result = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(BUCKET_NAME)
                        .build()
        );

        log.info("📂 [{}] 버킷 파일 목록 조회 (총 {}개)", BUCKET_NAME, result.contents().size());
        result.contents().forEach(object -> {
            log.info("- 파일명: {}, 크기: {} bytes", object.key(), object.size());
        });
    }

    @Test
    @DisplayName("스프링 빈 주입을 통한 Garage S3 연결 테스트")
    void springBeanS3Test() {
        log.info("🚀 Garage S3 업로드 테스트 시작");

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(TEST_KEY)
                            .build(),
                    RequestBody.fromString("Hello from Spring Boot Bean!"));
            log.info("✅ S3 업로드 성공!");
        } catch (Exception e) {
            log.error("❌ S3 업로드 중 에러 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test
    @DisplayName("Garage S3에서 파일 다운로드 및 내용 검증 테스트")
    void downloadS3Test() throws Exception {
        String expectedContent = "Hello from Spring Boot Bean!";

        log.info("🔍 파일 다운로드 시도 중... [Key: {}]", TEST_KEY);

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(TEST_KEY)
                        .build())) {

            String actualContent = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
            log.info("📄 읽어온 내용: {}", actualContent);

            assertEquals(expectedContent, actualContent, "내용 불일치");
            log.info("✅ 다운로드 및 검증 완료");
        }
    }

    @Test
    @DisplayName("Garage S3 파일 내용 수정(덮어쓰기) 테스트")
    void updateS3Test() throws Exception {
        String newContent = "Updated content in Garage!";

        log.info("🔄 파일 수정(덮어쓰기) 시작: {}", TEST_KEY);

        // 1. 동일한 Key로 새로운 내용 업로드
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(TEST_KEY)
                        .build(),
                RequestBody.fromString(newContent));

        // 2. 다시 가져와서 바뀌었는지 확인
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET_NAME).key(TEST_KEY).build())) {

            String actualContent = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
            log.info("📄 수정된 내용 확인: {}", actualContent);
            assertEquals(newContent, actualContent);
            log.info("✅ 수정 테스트 완료");
        }
    }

    @Test
    @DisplayName("Garage S3 파일 삭제 테스트")
    void deleteS3Test() {
        log.info("🗑️ 파일 삭제 시도: {}", TEST_KEY);

        // 1. 파일 삭제 실행
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(TEST_KEY)
                .build());

        // 2. 삭제 확인 (목록 조회 시 존재하지 않아야 함)
        ListObjectsV2Response result = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(BUCKET_NAME).build());

        boolean exists = result.contents().stream()
                .anyMatch(object -> object.key().equals(TEST_KEY));

        assertFalse(exists, "파일이 삭제되지 않았습니다.");
        log.info("✅ 삭제 성공 확인 완료");
    }
}