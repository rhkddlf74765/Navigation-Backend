package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

public record LineSegmentRow(
        long lineId,
        String highway,
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ,
        double cost,
        List<Point3D> geometry
) {
    public Point3D startPoint() {
        return new Point3D(startX, startY, startZ);
    }

    public Point3D endPoint() {
        return new Point3D(endX, endY, endZ);
    }
}
