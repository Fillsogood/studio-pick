package org.example.studiopick.application.studio;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {
  String upload(MultipartFile file);
  String upload(MultipartFile file, String dir); //
  void delete(String fileUrl);// 디렉토리 지정 가능
}
