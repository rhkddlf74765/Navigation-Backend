package com.example.campus_navigation_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "navigation.spatial-state"
)
public record SpatialStateProperties(
        int indoorConfirmCount,
        int outdoorConfirmCount,
        double boundaryUncertainMeters,
        double cvMinConfidence,
        long staleAfterSeconds
) {
    public SpatialStateProperties {
        if (indoorConfirmCount <= 0) {
            throw new IllegalArgumentException(
                    "indoorConfirmCount must be positive"
            );
        }

        if (outdoorConfirmCount <= 0) {
            throw new IllegalArgumentException(
                    "outdoorConfirmCount must be positive"
            );
        }

        if (!Double.isFinite(boundaryUncertainMeters)
                || boundaryUncertainMeters < 0.0) {
            throw new IllegalArgumentException(
                    "boundaryUncertainMeters must be non-negative"
            );
        }

        if (!Double.isFinite(cvMinConfidence)
                || cvMinConfidence < 0.0
                || cvMinConfidence > 1.0) {
            throw new IllegalArgumentException(
                    "cvMinConfidence must be between 0 and 1"
            );
        }

        if (staleAfterSeconds <= 0) {
            throw new IllegalArgumentException(
                    "staleAfterSeconds must be positive"
            );
        }
    }
}
