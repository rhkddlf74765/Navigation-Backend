package com.example.campus_navigation_backend.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO returned to the client after route search.
 *
 * @param routeSessionId session ID that identifies one route request
 * @param destinationBuildingName display name of the destination endpoint
 * @param selectedEntranceId destination-side graph endpoint node ID selected for the final route
 * @param totalDistanceMeters total route cost including approach and graph costs
 * @param approachDistanceMeters sum of start-side and destination-side approach costs
 * @param graphDistanceMeters A* path cost between the selected graph endpoint nodes
 * @param path full route path in internal metric coordinates
 */
public record RouteResponse(
        UUID routeSessionId,
        String destinationBuildingName,
        long selectedEntranceId,
        double totalDistanceMeters,
        double approachDistanceMeters,
        double graphDistanceMeters,
        List<RoutePoint> path
) {
}
