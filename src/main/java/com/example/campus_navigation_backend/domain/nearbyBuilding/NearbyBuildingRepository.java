package com.example.campus_navigation_backend.domain.nearbyBuilding;

import java.util.List;

public interface NearbyBuildingRepository {
    List<NearbyBuilding> findNearby(
            double latitude,
            double longitude,
            double radiusMeters,
            int limit
    );
}
