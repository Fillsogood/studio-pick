package org.example.studiopick.application.admin.dto;

public record AdminStudioOwnerInfo(
    Long id,
    String name,
    String email,
    String phone,
    String status
) {}