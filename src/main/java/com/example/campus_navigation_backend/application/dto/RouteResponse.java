package com.example.campus_navigation_backend.application.dto;

import java.util.List;
import java.util.UUID;

public record RouteResponse(
        UUID routeSessionId,
        double totalDistanceMeters,
        RouteExpectedTime expectedTime,
        List<RoutePoint> path
) {
    public RouteResponse {
        path =
                List.copyOf(path);
    }
}