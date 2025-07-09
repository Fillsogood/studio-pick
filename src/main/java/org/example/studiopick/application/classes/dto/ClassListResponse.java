package org.example.studiopick.application.classes.dto;

import java.util.List;

public record ClassListResponse(
    List<ClassListDto> classes
) {}
