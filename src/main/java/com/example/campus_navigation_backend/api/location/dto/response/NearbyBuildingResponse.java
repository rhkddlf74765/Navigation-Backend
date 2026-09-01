package com.example.campus_navigation_backend.api.location.dto.response;

import com.example.campus_navigation_backend.api.location.BuildingDescription;
import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuilding;

public record NearbyBuildingResponse(
        Long buildingId,
        String name,
        BuildingDescription description,
        double lat,
        double lon,
        Double ele,
        double distanceMeters
) {

    public static NearbyBuildingResponse from(
            NearbyBuilding building
    ) {

        return new NearbyBuildingResponse(
                building.buildingId(),
                building.name(),

                // DB description 추가 전까지 null
                null,

                building.lat(),
                building.lon(),
                building.ele(),
                building.distanceMeters()
        );
    }
}
