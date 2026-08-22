package com.example.campus_navigation_backend.application.dto;

import java.util.List;
import java.util.UUID;

public record RouteResponse(
        UUID routeSessionId,
        String destinationBuildingName,
        Long selectedEntranceId,
        double totalDistanceMeters,
        double totalCost,
        RouteExpectedTime expectedTime,
        double approachDistanceMeters,
        double approachCost,
        double graphDistanceMeters,
        double graphCost,
        List<RoutePoint> path
) {
    public RouteResponse {
        path =
                List.copyOf(path);
    }
}