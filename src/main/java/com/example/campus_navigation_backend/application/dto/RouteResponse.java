package com.example.campus_navigation_backend.application.dto;

public record RouteResponse(
        String destinationBuildingName,
        long selectedEntranceId,
        double totalDistanceMeters,
        double approachDistanceMeters,
        double graphDistanceMeters,
        List<RoutePoint> path
) {
}
