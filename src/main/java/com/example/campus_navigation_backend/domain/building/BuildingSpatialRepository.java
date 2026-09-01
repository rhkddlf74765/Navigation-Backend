package com.example.campus_navigation_backend.domain.building;

import java.util.Optional;
import java.util.OptionalDouble;

public interface BuildingSpatialRepository {

    Optional<BuildingRef> findByName(
            String buildingName
    );

    Optional<ContainingBuilding> findContaining(
            double lat,
            double lon
    );

    OptionalDouble findNearestDistanceMeters(
            double lat,
            double lon
    );
}
