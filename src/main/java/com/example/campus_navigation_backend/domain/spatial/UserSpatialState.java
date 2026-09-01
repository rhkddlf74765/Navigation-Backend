package com.example.campus_navigation_backend.domain.spatial;

import java.time.Instant;
import java.util.UUID;

public record UserSpatialState(
        UUID userId,
        EnvironmentState stableState,
        Long buildingId,
        EnvironmentState pendingState,
        int consecutiveCount,
        Instant updatedAt
) {
    public UserSpatialState {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id is required."
            );
        }

        if (stableState == null) {
            stableState = EnvironmentState.UNCERTAIN;
        }

        if (pendingState == null) {
            pendingState = EnvironmentState.UNCERTAIN;
        }

        if (consecutiveCount < 0) {
            throw new IllegalArgumentException(
                    "Consecutive count must not be negative."
            );
        }

        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}
