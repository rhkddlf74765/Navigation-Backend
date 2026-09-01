package com.example.campus_navigation_backend.service.spatial;

import com.example.campus_navigation_backend.config.SpatialStateProperties;
import com.example.campus_navigation_backend.domain.building.BuildingRef;
import com.example.campus_navigation_backend.domain.building.BuildingSpatialRepository;
import com.example.campus_navigation_backend.domain.building.ContainingBuilding;
import com.example.campus_navigation_backend.domain.spatial.EnvironmentState;
import com.example.campus_navigation_backend.domain.spatial.SpatialEvidence;
import com.example.campus_navigation_backend.domain.spatial.UserSpatialState;
import com.example.campus_navigation_backend.service.building.BuildingSpatialQueryService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpatialStatePolicyTest {

    private static final UUID USER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    private static final Instant NOW =
            Instant.parse(
                    "2026-09-01T00:00:00Z"
            );

    @Test
    void gpsNearBuildingBoundaryYieldsUncertain() {
        FakeBuildingSpatialRepository repository =
                new FakeBuildingSpatialRepository();
        repository.containing =
                Optional.of(
                        new ContainingBuilding(
                                3L,
                                "Boundary Building",
                                1.0
                        )
                );

        SpatialStatePolicy policy =
                policy(repository);

        SpatialEvidence evidence =
                policy.classifyGps(
                        37.0,
                        127.0,
                        NOW
                );

        assertThat(evidence.state())
                .isEqualTo(
                        EnvironmentState.UNCERTAIN
                );
        assertThat(evidence.buildingId())
                .isEqualTo(3L);
    }

    @Test
    void twoIndoorEvidencesTransitionToIndoor() {
        SpatialStatePolicy policy =
                policy(
                        new FakeBuildingSpatialRepository()
                );

        UserSpatialState first =
                policy.apply(
                        USER_ID,
                        null,
                        new SpatialEvidence(
                                EnvironmentState.INDOOR,
                                7L,
                                1.0,
                                NOW
                        )
                );

        UserSpatialState second =
                policy.apply(
                        USER_ID,
                        first,
                        new SpatialEvidence(
                                EnvironmentState.INDOOR,
                                7L,
                                1.0,
                                NOW.plusSeconds(1)
                        )
                );

        assertThat(first.stableState())
                .isEqualTo(
                        EnvironmentState.UNCERTAIN
                );
        assertThat(second.stableState())
                .isEqualTo(
                        EnvironmentState.INDOOR
                );
        assertThat(second.buildingId())
                .isEqualTo(7L);
    }

    @Test
    void twoOutdoorEvidencesTransitionToOutdoor() {
        SpatialStatePolicy policy =
                policy(
                        new FakeBuildingSpatialRepository()
                );

        UserSpatialState first =
                policy.apply(
                        USER_ID,
                        null,
                        new SpatialEvidence(
                                EnvironmentState.OUTDOOR,
                                null,
                                1.0,
                                NOW
                        )
                );

        UserSpatialState second =
                policy.apply(
                        USER_ID,
                        first,
                        new SpatialEvidence(
                                EnvironmentState.OUTDOOR,
                                null,
                                1.0,
                                NOW.plusSeconds(1)
                        )
                );

        assertThat(first.stableState())
                .isEqualTo(
                        EnvironmentState.UNCERTAIN
                );
        assertThat(second.stableState())
                .isEqualTo(
                        EnvironmentState.OUTDOOR
                );
        assertThat(second.buildingId())
                .isNull();
    }

    private SpatialStatePolicy policy(
            BuildingSpatialRepository repository
    ) {
        return new SpatialStatePolicy(
                new BuildingSpatialQueryService(
                        repository
                ),
                new SpatialStateProperties(
                        2,
                        2,
                        3.0,
                        0.7,
                        120
                )
        );
    }

    private static final class
    FakeBuildingSpatialRepository
            implements BuildingSpatialRepository {

        private Optional<ContainingBuilding>
                containing =
                Optional.empty();

        @Override
        public Optional<BuildingRef> findByName(
                String buildingName
        ) {
            return Optional.empty();
        }

        @Override
        public Optional<ContainingBuilding> findContaining(
                double lat,
                double lon
        ) {
            return containing;
        }

        @Override
        public OptionalDouble findNearestDistanceMeters(
                double lat,
                double lon
        ) {
            return OptionalDouble.of(10.0);
        }
    }
}
