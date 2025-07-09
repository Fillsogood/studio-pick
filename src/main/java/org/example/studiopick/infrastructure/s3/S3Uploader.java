package org.example.studiopick.infrastructure.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final String bucket = "studio-pick"; // 버킷 이름 하드코딩 or yml에서 주입 가능

    public String upload(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID(); // 예: profile/uuid
        String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패: " + e.getMessage());
        }

        return fileUrl;
    }
}

