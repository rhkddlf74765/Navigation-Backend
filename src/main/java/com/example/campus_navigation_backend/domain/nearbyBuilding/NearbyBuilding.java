package com.example.campus_navigation_backend.domain.nearbyBuilding;

public record NearbyBuilding(
        Long buildingId,
        String name,
        String description,
        double lat,
        double lon,
        Double ele,
        double distanceMeters

) {
    public NearbyBuilding {

        if (buildingId == null) {
            throw new IllegalArgumentException(
                    "buildingId must not be null"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "building name must not be blank"
            );
        }

        if (!Double.isFinite(lat)) {
            throw new IllegalArgumentException(
                    "latitude must be finite"
            );
        }

        if (!Double.isFinite(lon)) {
            throw new IllegalArgumentException(
                    "longitude must be finite"
            );
        }

        if (ele != null
                && !Double.isFinite(ele)) {
            throw new IllegalArgumentException(
                    "elevation must be finite"
            );
        }

        if (!Double.isFinite(distanceMeters)
                || distanceMeters < 0.0) {

            throw new IllegalArgumentException(
                    "distanceMeters must be non-negative"
            );
        }
    }
}
