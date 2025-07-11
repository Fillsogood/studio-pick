package org.example.studiopick.application.studio;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {
  String upload(MultipartFile file);
}
