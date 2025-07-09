package org.example.studiopick.domain.common.dto;

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

  public boolean isSuccess() {
    return success;
  }

  public T getData() {
    return data;
  }
}