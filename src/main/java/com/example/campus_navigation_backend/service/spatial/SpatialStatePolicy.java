package com.example.campus_navigation_backend.service.spatial;

import com.example.campus_navigation_backend.config.SpatialStateProperties;
import com.example.campus_navigation_backend.domain.building.ContainingBuilding;
import com.example.campus_navigation_backend.domain.spatial.EnvironmentState;
import com.example.campus_navigation_backend.domain.spatial.SpatialEvidence;
import com.example.campus_navigation_backend.domain.spatial.UserSpatialState;
import com.example.campus_navigation_backend.service.building.BuildingSpatialQueryService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Component
public class SpatialStatePolicy {

    private final BuildingSpatialQueryService
            buildingSpatialQueryService;

    private final SpatialStateProperties properties;

    public SpatialStatePolicy(
            BuildingSpatialQueryService buildingSpatialQueryService,
            SpatialStateProperties properties
    ) {
        this.buildingSpatialQueryService =
                buildingSpatialQueryService;
        this.properties = properties;
    }

    public SpatialEvidence classifyGps(
            double lat,
            double lon,
            Instant observedAt
    ) {
        Instant effectiveObservedAt =
                observedAt == null
                        ? Instant.now()
                        : observedAt;

        Optional<ContainingBuilding> containing =
                buildingSpatialQueryService
                        .findContaining(
                                lat,
                                lon
                        );

        if (containing.isPresent()) {
            ContainingBuilding building =
                    containing.get();

            if (building.distanceToBoundaryMeters()
                    > properties
                    .boundaryUncertainMeters()) {
                return new SpatialEvidence(
                        EnvironmentState.INDOOR,
                        building.id(),
                        1.0,
                        effectiveObservedAt
                );
            }

            return new SpatialEvidence(
                    EnvironmentState.UNCERTAIN,
                    building.id(),
                    1.0,
                    effectiveObservedAt
            );
        }

        OptionalDouble nearestDistance =
                buildingSpatialQueryService
                        .findNearestDistanceMeters(
                                lat,
                                lon
                        );

        if (nearestDistance.isPresent()
                && nearestDistance.getAsDouble()
                <= properties
                .boundaryUncertainMeters()) {
            return new SpatialEvidence(
                    EnvironmentState.UNCERTAIN,
                    null,
                    1.0,
                    effectiveObservedAt
            );
        }

        return new SpatialEvidence(
                EnvironmentState.OUTDOOR,
                null,
                1.0,
                effectiveObservedAt
        );
    }

    public UserSpatialState apply(
            UUID userId,
            UserSpatialState previous,
            SpatialEvidence evidence
    ) {
        UserSpatialState base =
                staleOrMissing(
                        previous,
                        evidence.observedAt()
                )
                        ? initialState(
                                userId,
                                evidence.observedAt()
                        )
                        : previous;

        if (evidence.state()
                == EnvironmentState.UNCERTAIN) {
            return new UserSpatialState(
                    userId,
                    base.stableState(),
                    base.buildingId(),
                    EnvironmentState.UNCERTAIN,
                    0,
                    evidence.observedAt()
            );
        }

        int consecutiveCount =
                evidence.state()
                        == base.pendingState()
                        ? base.consecutiveCount() + 1
                        : 1;

        int requiredCount =
                evidence.state()
                        == EnvironmentState.INDOOR
                        ? properties.indoorConfirmCount()
                        : properties.outdoorConfirmCount();

        if (consecutiveCount < requiredCount) {
            return new UserSpatialState(
                    userId,
                    base.stableState(),
                    base.buildingId(),
                    evidence.state(),
                    consecutiveCount,
                    evidence.observedAt()
            );
        }

        return new UserSpatialState(
                userId,
                evidence.state(),
                evidence.state()
                        == EnvironmentState.INDOOR
                        ? evidence.buildingId()
                        : null,
                evidence.state(),
                consecutiveCount,
                evidence.observedAt()
        );
    }

    private boolean staleOrMissing(
            UserSpatialState previous,
            Instant observedAt
    ) {
        if (previous == null) {
            return true;
        }

        return Duration.between(
                        previous.updatedAt(),
                        observedAt
                )
                .getSeconds()
                > properties.staleAfterSeconds();
    }

    private UserSpatialState initialState(
            UUID userId,
            Instant observedAt
    ) {
        return new UserSpatialState(
                userId,
                EnvironmentState.UNCERTAIN,
                null,
                EnvironmentState.UNCERTAIN,
                0,
                observedAt
        );
    }
}
