package com.example.campus_navigation_backend.domain.building;

public record ContainingBuilding(
        long id,
        String name,
        double distanceToBoundaryMeters
) {
    public ContainingBuilding {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Building id must be positive."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Building name must not be blank."
            );
        }

        if (!Double.isFinite(distanceToBoundaryMeters)
                || distanceToBoundaryMeters < 0.0) {
            throw new IllegalArgumentException(
                    "Distance to boundary must be non-negative."
            );
        }
    }
}
