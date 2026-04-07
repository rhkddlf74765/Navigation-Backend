package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

public record CurrentProjectionRow (
        long lineId,
        double fraction,
        double projectionX,
        double projectionY,
        double projectionZ,
        double connectorCost,
        double leftCost,
        double rightCost,
        List<Point3D> connectorGeometry,
        List<Point3D> leftGeometry,
        List<Point3D> rightGeometry
){
    public Point3D projectionPoint() {
        return new Point3D(projectionX, projectionY, projectionZ);
    }
}
