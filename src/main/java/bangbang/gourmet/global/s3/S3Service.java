package bangbang.gourmet.global.s3;

import bangbang.gourmet.common.exception.model.S3UploadException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.common.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public String uploadImage(MultipartFile file, String bucketName, String dirName) {
        FileValidator.validateImageFile(file);

        // 2. 검증 통과 시 기존 업로드 로직 실행
        return upload(file, bucketName, dirName);
    }

    private String upload(MultipartFile file, String bucketName, String dirName) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = dirName + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");

        try{
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return fileName;
        } catch(IOException e){
            throw new S3UploadException(ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    public String uploadImageFromUrl(String imageUrl, String bucketName, String dirName, String referer) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(imageUrl).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Referer", referer);
            conn.connect();

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new S3UploadException(ErrorCode.S3_UPLOAD_ERROR);
            }

            String mimeType = contentType.split(";")[0].trim();
            String ext = switch (mimeType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };

            byte[] imageBytes;
            try (InputStream is = conn.getInputStream()) {
                imageBytes = is.readAllBytes();
            }

            String fileName = dirName + "/" + UUID.randomUUID() + ext;

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(mimeType)
                    .build(),
                    RequestBody.fromBytes(imageBytes));

            return fileName;
        } catch (IOException e) {
            throw new S3UploadException(ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    public void delete(String bucketName, String key){
        if (key == null || key.isBlank()) return;
        s3Client.deleteObject(DeleteObjectRequest.builder().
                bucket(bucketName).
                key(key).
                build());
    }
}
