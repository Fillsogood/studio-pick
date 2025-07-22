package org.example.studiopick.domain.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiSuccessResponse<T> {
  private final boolean success = true;
  private final T data;
  private String message;

  public ApiSuccessResponse(T data) {
    this.data = data;
  }

  public ApiSuccessResponse(T data, String message) {
    this.data = data;
    this.message = message;
  }

  public static <T> ApiSuccessResponse<T> of(T data) {
    return new ApiSuccessResponse<>(data);
  }

  public static <T> ApiSuccessResponse<T> of(T data, String message) {
    return new ApiSuccessResponse<>(data, message);
  }

}