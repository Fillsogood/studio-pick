package org.example.studiopick.application.admin.dto;

import java.util.List;

public record AdminStudioListResponse(
    List<AdminStudioResponse> studios,
    AdminPaginationResponse pagination
) {}
