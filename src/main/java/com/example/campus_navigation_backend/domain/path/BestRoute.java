package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

public record BestRoute (
        long selectedEntranceId,
        double totalDistanceMeters,
        double approachDistanceMeters,
        double graphDistanceMeters,
        List<Point3D> path
){
}
