package com.example.campus_navigation_backend.service.spatial;

import com.example.campus_navigation_backend.config.SpatialStateProperties;
import com.example.campus_navigation_backend.domain.spatial.EnvironmentState;
import com.example.campus_navigation_backend.domain.spatial.SpatialEvidence;
import com.example.campus_navigation_backend.domain.spatial.UserSpatialState;
import com.example.campus_navigation_backend.domain.spatial.UserSpatialStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSpatialStateService {

    private final UserSpatialStateStore store;

    private final SpatialStatePolicy policy;

    private final SpatialStateProperties properties;

    public UserSpatialStateService(
            UserSpatialStateStore store,
            SpatialStatePolicy policy,
            SpatialStateProperties properties
    ) {
        this.store = store;
        this.policy = policy;
        this.properties = properties;
    }

    public Optional<UserSpatialState> findByUserId(
            UUID userId
    ) {
        return store.findByUserId(
                userId
        );
    }

    public UserSpatialState updateFromGps(
            UUID userId,
            double lat,
            double lon,
            Instant observedAt
    ) {
        SpatialEvidence evidence =
                policy.classifyGps(
                        lat,
                        lon,
                        observedAt
                );

        return updateFromEvidence(
                userId,
                evidence
        );
    }

    public UserSpatialState updateFromCv(
            UUID userId,
            EnvironmentState state,
            Long buildingId,
            double confidence,
            Instant observedAt
    ) {
        EnvironmentState effectiveState =
                confidence
                        >= properties.cvMinConfidence()
                        ? state
                        : EnvironmentState.UNCERTAIN;

        SpatialEvidence evidence =
                new SpatialEvidence(
                        effectiveState,
                        buildingId,
                        confidence,
                        observedAt
                );

        return updateFromEvidence(
                userId,
                evidence
        );
    }

    private UserSpatialState updateFromEvidence(
            UUID userId,
            SpatialEvidence evidence
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id is required."
            );
        }

        return store.update(
                userId,
                previous ->
                        policy.apply(
                                userId,
                                previous,
                                evidence
                        )
        );
    }
}
