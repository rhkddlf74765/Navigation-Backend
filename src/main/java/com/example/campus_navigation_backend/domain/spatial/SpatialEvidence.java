package com.example.campus_navigation_backend.domain.spatial;

import java.time.Instant;

public record SpatialEvidence(
        EnvironmentState state,
        Long buildingId,
        double confidence,
        Instant observedAt
) {
    public SpatialEvidence {
        if (state == null) {
            throw new IllegalArgumentException(
                    "Environment state is required."
            );
        }

        if (!Double.isFinite(confidence)
                || confidence < 0.0
                || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "Confidence must be between 0 and 1."
            );
        }

        if (observedAt == null) {
            observedAt = Instant.now();
        }
    }
}
