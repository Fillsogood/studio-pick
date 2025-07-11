package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploaderImpl implements FileUploader {

  // 로컬용
//  @Override
//  public String upload(MultipartFile file) {
//
//    return "https://dummy.com/" + UUID.randomUUID();
//  }

  // 나중에 S3 사용할 때
    private final S3Uploader s3Uploader;

    @Override
    public String upload(MultipartFile file) {
      return s3Uploader.upload(file, "studio"); // 예: studio 디렉터리에 저장
    }
}
