package com.example.campus_navigation_backend.application.nearbyBuilding;

import com.example.campus_navigation_backend.config.NearbyBuildingProperties;
import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuilding;
import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NearbyBuildingService {

    private final NearbyBuildingRepository repository;
    private final NearbyBuildingProperties properties;

    public NearbyBuildingService(
            NearbyBuildingRepository repository,
            NearbyBuildingProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    public List<NearbyBuilding> findNearby(
            double latitude,
            double longitude
    ) {

        validateCoordinate(
                latitude,
                longitude
        );

        return repository.findNearby(
                latitude,
                longitude,
                properties.searchRadiusMeters(),
                properties.maxResults()
        );
    }

    private void validateCoordinate(
            double latitude,
            double longitude
    ) {

        if (!Double.isFinite(latitude)
                || latitude < -90.0
                || latitude > 90.0) {

            throw new IllegalArgumentException(
                    "Invalid latitude: "
                            + latitude
            );
        }

        if (!Double.isFinite(longitude)
                || longitude < -180.0
                || longitude > 180.0) {

            throw new IllegalArgumentException(
                    "Invalid longitude: "
                            + longitude
            );
        }
    }
}