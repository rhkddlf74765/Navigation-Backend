package com.example.campus_navigation_backend.service.building;

import com.example.campus_navigation_backend.domain.building.BuildingRef;
import com.example.campus_navigation_backend.domain.building.BuildingSpatialRepository;
import com.example.campus_navigation_backend.domain.building.ContainingBuilding;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalDouble;

@Service
public class BuildingSpatialQueryService {

    private final BuildingSpatialRepository repository;

    public BuildingSpatialQueryService(
            BuildingSpatialRepository repository
    ) {
        this.repository = repository;
    }

    public Optional<BuildingRef> findByName(
            String buildingName
    ) {
        if (buildingName == null
                || buildingName.isBlank()) {
            return Optional.empty();
        }

        return repository.findByName(
                buildingName.trim()
        );
    }

    public Optional<ContainingBuilding> findContaining(
            double lat,
            double lon
    ) {
        validateCoordinate(
                lat,
                lon
        );

        return repository.findContaining(
                lat,
                lon
        );
    }

    public OptionalDouble findNearestDistanceMeters(
            double lat,
            double lon
    ) {
        validateCoordinate(
                lat,
                lon
        );

        return repository.findNearestDistanceMeters(
                lat,
                lon
        );
    }

    private void validateCoordinate(
            double lat,
            double lon
    ) {
        if (!Double.isFinite(lat)
                || lat < -90.0
                || lat > 90.0) {
            throw new IllegalArgumentException(
                    "Invalid latitude: "
                            + lat
            );
        }

        if (!Double.isFinite(lon)
                || lon < -180.0
                || lon > 180.0) {
            throw new IllegalArgumentException(
                    "Invalid longitude: "
                            + lon
            );
        }
    }
}
