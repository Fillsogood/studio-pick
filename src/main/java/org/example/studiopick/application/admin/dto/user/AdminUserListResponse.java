package org.example.studiopick.application.admin.dto.user;

import org.example.studiopick.application.admin.dto.studio.AdminPaginationResponse;

import java.util.List;

public record AdminUserListResponse(
    List<AdminUserResponse> users,
    AdminPaginationResponse pagination
) {}