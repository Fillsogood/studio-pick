package org.example.studiopick.application.workshop;

import org.example.studiopick.application.workshop.dto.WorkShopDetailDto;
import org.example.studiopick.application.workshop.dto.WorkShopListResponse;

public interface WorkShopService {
  WorkShopListResponse getClassList(Long studioId, String status, String date);
  WorkShopDetailDto getClassDetail(Long classId);
}
