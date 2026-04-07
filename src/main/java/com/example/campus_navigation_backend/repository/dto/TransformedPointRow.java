package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

public record TransformedPointRow (
        double x,
        double y,
        double z
){
    public Point3D toPoint3D() {
        return new Point3D(x, y, z);
    }
}
