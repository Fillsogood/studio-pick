package org.example.studiopick.application.review.dto;

public record ReviewUpdateRequest(
    Short rating,
    String comment,
    String imageUrl
) {}
