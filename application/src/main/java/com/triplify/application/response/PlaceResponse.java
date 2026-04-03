package com.triplify.application.response;

public record PlaceResponse(
        Integer id,
        String name,
        String country,
        String date,
        PlaceStatus status,
        String imageUrl
) {}
