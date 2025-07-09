package org.example.studiopick.application.studio.dto;

public record StudioApplicationRequest(
    String name,
    String description,
    String location,
    String phone
) {}
