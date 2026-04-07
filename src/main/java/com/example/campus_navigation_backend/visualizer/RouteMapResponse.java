package com.example.campus_navigation_backend.visualizer;

import java.util.List;

public record RouteMapResponse(
        String destinationBuildingName,
        long selectedEntranceId,
        double totalDistanceMeters,
        double approachDistanceMeters,
        double graphDistanceMeters,
        MapPoint startPoint,
        List<MapPoint> path
){
}
