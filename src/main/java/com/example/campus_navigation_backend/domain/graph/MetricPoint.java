package com.example.campus_navigation_backend.domain.graph;

public record MetricPoint(
        double x,
        double y,
        double z
) {
    public MetricPoint {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(z)) {
            throw new IllegalArgumentException(
                    "Metric point coordinates must be finite."
            );
        }
    }

    public double distance2D(MetricPoint other) {
        return Math.hypot(
                x - other.x,
                y - other.y
        );
    }

    public double distance3D(MetricPoint other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;

        return Math.sqrt(
                dx * dx
                        + dy * dy
                        + dz * dz
        );
    }
}
