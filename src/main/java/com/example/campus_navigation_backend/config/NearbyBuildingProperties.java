package com.example.campus_navigation_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "navigation.nearby-building"
)
public record NearbyBuildingProperties(
        double searchRadiusMeters,
        int maxResults
) {

    public NearbyBuildingProperties {

        if (!Double.isFinite(searchRadiusMeters)
                || searchRadiusMeters <= 0.0) {

            throw new IllegalArgumentException(
                    "searchRadiusMeters must be positive"
            );
        }

        if (maxResults <= 0) {

            throw new IllegalArgumentException(
                    "maxResults must be positive"
            );
        }
    }
}
