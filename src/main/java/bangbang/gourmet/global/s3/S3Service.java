package bangbang.gourmet.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public String upload(MultipartFile file, String bucketName, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try{
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return fileName;
        } catch(IOException e){
            throw new RuntimeException("S3업로드 에러",e);
        }
    }

    public void delete(String bucketName, String key){
        if (key == null || key.isBlank()) return;
        s3Client.deleteObject(DeleteObjectRequest.builder().
                bucket(bucketName).
                key(key).
                build());
    }

    public String generateProfileUrl(String bucketName, String key){
        if (key == null || key.isBlank()) return null;
        // 형식: /버킷명/경로
        return String.format("%s/%s", bucketName, key);
    }
}
