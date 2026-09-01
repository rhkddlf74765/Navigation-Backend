package com.example.campus_navigation_backend.domain.building;

public record BuildingRef(
        long id,
        String name
) {
    public BuildingRef {
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
    }
}
