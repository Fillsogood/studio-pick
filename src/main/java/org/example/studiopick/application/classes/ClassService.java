package org.example.studiopick.application.classes;

import org.example.studiopick.application.classes.dto.ClassDetailDto;
import org.example.studiopick.application.classes.dto.ClassListResponse;
import org.example.studiopick.application.classes.dto.ClassReservationResponse;
import org.example.studiopick.application.classes.dto.UserClassReservationListResponse;

public interface ClassService {
  ClassListResponse getClassList(Long studioId, String status, String date);
  ClassDetailDto getClassDetail(Long classId);
  ClassReservationResponse reserveClass(Long classId, Long userId, int participants);
}
