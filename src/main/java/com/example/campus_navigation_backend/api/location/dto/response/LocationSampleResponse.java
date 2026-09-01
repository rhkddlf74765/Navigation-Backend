package com.example.campus_navigation_backend.api.location.dto.response;

import java.time.Instant;
import java.util.List;

public record LocationSampleResponse(
        Instant locationRecordedAt,
        List<NearbyBuildingResponse> nearbyBuildings
) {

    public LocationSampleResponse {
        nearbyBuildings =
                List.copyOf(
                        nearbyBuildings
                );
    }
}
