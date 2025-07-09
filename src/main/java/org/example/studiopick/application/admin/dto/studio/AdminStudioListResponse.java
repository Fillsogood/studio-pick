package org.example.studiopick.application.admin.dto.studio;

import java.util.List;

public record AdminStudioListResponse(
    List<AdminStudioResponse> studios,
    AdminPaginationResponse pagination
) {}
