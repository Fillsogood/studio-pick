package org.example.studiopick.domain.common.dto;

public class ApiSuccessResponse<T> {
  private final boolean success = true;
  private final T data;

  public ApiSuccessResponse(T data) {
    this.data = data;
  }

  public boolean isSuccess() {
    return success;
  }

  public T getData() {
    return data;
  }
}