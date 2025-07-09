package org.example.studiopick.application.admin.dto.studio;

public record AdminStudioOwnerInfo(
    Long id,
    String name,
    String email,
    String phone,
    String status
) {}