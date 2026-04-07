package com.example.campus_navigation_backend.domain.graph;

import java.util.List;

public record GraphEdge (
        long fromNodeId,
        long toNodeId,
        double cost,
        String edgeType,
        List<Point3D> geometry
){
}
