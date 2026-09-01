package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.domain.building.BuildingRef;
import com.example.campus_navigation_backend.domain.building.BuildingSpatialRepository;
import com.example.campus_navigation_backend.domain.building.ContainingBuilding;
import com.example.campus_navigation_backend.domain.geo.CoordinateTransformer;
import com.example.campus_navigation_backend.domain.geo.GeoPoint;
import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.EdgePosition;
import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import com.example.campus_navigation_backend.domain.projection.EdgeProjection;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import com.example.campus_navigation_backend.service.building.BuildingSpatialQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteRequestResolverSpatialTest {

    private static final UUID USER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000001"
            );

    @Test
    void coordinateOutsideBuildingUsesEdgeProjection() {
        CampusGraphStore graphStore =
                initializedEmptyGraphStore();

        FakeBuildingSpatialRepository repository =
                new FakeBuildingSpatialRepository();

        FakeCoordinateTransformer transformer =
                new FakeCoordinateTransformer();

        RouteRequestResolver resolver =
                new RouteRequestResolver(
                        graphStore,
                        transformer,
                        new FakeEdgeProjectionFinder(),
                        new BuildingSpatialQueryService(
                                repository
                        )
                );

        ResolvedRouteRequest resolved =
                resolver.resolve(
                        new RouteRequest(
                                USER_ID,
                                RouteEndpointRequest.coordinate(
                                        127.0,
                                        37.0,
                                        null
                                ),
                                RouteEndpointRequest.coordinate(
                                        127.1,
                                        37.1,
                                        null
                                )
                        )
                );

        assertThat(resolved.start())
                .isInstanceOf(
                        ResolvedRouteEndpoint.Coordinate.class
                );
        assertThat(transformer.toMetricCallCount)
                .isEqualTo(2);
    }

    @Test
    void coordinateInsideBuildingResolvesAsBuildingEndpoint() {
        CampusGraphStore graphStore =
                initializedGraphStoreWithEntrance();

        FakeBuildingSpatialRepository repository =
                new FakeBuildingSpatialRepository();
        repository.containing =
                Optional.of(
                        new ContainingBuilding(
                                7L,
                                "Engineering Building",
                                10.0
                        )
                );
        repository.byName =
                Optional.of(
                        new BuildingRef(
                                7L,
                                "Engineering Building"
                        )
                );

        FakeCoordinateTransformer transformer =
                new FakeCoordinateTransformer();

        RouteRequestResolver resolver =
                new RouteRequestResolver(
                        graphStore,
                        transformer,
                        new FakeEdgeProjectionFinder(),
                        new BuildingSpatialQueryService(
                                repository
                        )
                );

        ResolvedRouteRequest resolved =
                resolver.resolve(
                        new RouteRequest(
                                USER_ID,
                                RouteEndpointRequest.coordinate(
                                        127.0,
                                        37.0,
                                        null
                                ),
                                RouteEndpointRequest.building(
                                        "Engineering Building"
                                )
                        )
                );

        assertThat(resolved.start())
                .isInstanceOf(
                        ResolvedRouteEndpoint.Building.class
                );
        assertThat(
                ((ResolvedRouteEndpoint.Building)
                        resolved.start())
                        .entranceNodeIds()
        ).containsExactly(10L);
        assertThat(transformer.toMetricCallCount)
                .isZero();
    }

    @Test
    void buildingWithoutRoutableEntranceFailsClearly() {
        CampusGraphStore graphStore =
                initializedEmptyGraphStore();

        FakeBuildingSpatialRepository repository =
                new FakeBuildingSpatialRepository();
        repository.byName =
                Optional.of(
                        new BuildingRef(
                                7L,
                                "Engineering Building"
                        )
                );

        RouteRequestResolver resolver =
                new RouteRequestResolver(
                        graphStore,
                        new FakeCoordinateTransformer(),
                        new FakeEdgeProjectionFinder(),
                        new BuildingSpatialQueryService(
                                repository
                        )
                );

        assertThatThrownBy(
                () ->
                        resolver.resolve(
                                new RouteRequest(
                                        USER_ID,
                                        RouteEndpointRequest.building(
                                                "Engineering Building"
                                        ),
                                        RouteEndpointRequest.coordinate(
                                                127.0,
                                                37.0,
                                                null
                                        )
                                )
                        )
        ).isInstanceOf(
                IllegalArgumentException.class
        ).hasMessageContaining(
                "No routable entrance found for building"
        );
    }

    private CampusGraphStore initializedGraphStoreWithEntrance() {
        CampusGraph.Builder builder =
                CampusGraph.builder();
        builder.addNode(
                10L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(0.0, 0.0, 0.0)
        );
        builder.addBuildingEntrance(
                7L,
                "Engineering Building",
                10L
        );

        CampusGraphStore store =
                new CampusGraphStore();
        store.initialize(
                builder.build()
        );
        return store;
    }

    private CampusGraphStore initializedEmptyGraphStore() {
        CampusGraph.Builder builder =
                CampusGraph.builder();
        builder.addNode(
                1L,
                GraphNodeType.BASE,
                new MetricPoint(0.0, 0.0, 0.0)
        );

        CampusGraphStore store =
                new CampusGraphStore();
        store.initialize(
                builder.build()
        );
        return store;
    }

    private static final class
    FakeBuildingSpatialRepository
            implements BuildingSpatialRepository {

        private Optional<BuildingRef> byName =
                Optional.empty();

        private Optional<ContainingBuilding>
                containing =
                Optional.empty();

        @Override
        public Optional<BuildingRef> findByName(
                String buildingName
        ) {
            return byName;
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

    private static final class
    FakeCoordinateTransformer
            implements CoordinateTransformer {

        private int toMetricCallCount;

        @Override
        public MetricPoint toMetric(
                GeoPoint point
        ) {
            toMetricCallCount++;
            return new MetricPoint(
                    point.lon(),
                    point.lat(),
                    point.ele()
            );
        }

        @Override
        public GeoPoint toWgs84(
                MetricPoint point
        ) {
            return new GeoPoint(
                    point.x(),
                    point.y(),
                    point.z()
            );
        }
    }

    private static final class
    FakeEdgeProjectionFinder
            extends EdgeProjectionFinder {

        private FakeEdgeProjectionFinder() {
            super(
                    null,
                    new EdgeSpatialIndex(null),
                    new PointProjector()
            );
        }

        @Override
        public List<EdgeProjection> findCandidates(
                MetricPoint point
        ) {
            PhysicalEdge edge =
                    PhysicalEdge.create(
                            1L,
                            1L,
                            2L,
                            10.0,
                            "footway",
                            List.of(
                                    new MetricPoint(
                                            0.0,
                                            0.0,
                                            0.0
                                    ),
                                    new MetricPoint(
                                            10.0,
                                            0.0,
                                            0.0
                                    )
                            ),
                            (highway, distanceMeters, elevationDelta) ->
                                    distanceMeters
                    );

            return List.of(
                    new EdgeProjection(
                            edge,
                            new MetricPoint(
                                    0.0,
                                    0.0,
                                    0.0
                            ),
                            new EdgePosition(
                                    0,
                                    0.0,
                                    0.0
                            ),
                            0.0
                    )
            );
        }
    }
}
