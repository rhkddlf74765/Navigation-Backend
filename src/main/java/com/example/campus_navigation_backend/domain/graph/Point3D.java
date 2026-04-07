package com.example.campus_navigation_backend.domain.graph;

public record Point3D(
        double x, double y, double z
) {
    public double distance2D(Point3D other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    public double distance3D(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
