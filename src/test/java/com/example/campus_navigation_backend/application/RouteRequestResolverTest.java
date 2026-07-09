package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteEndpointType;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.building.BuildingPointStore;
import com.example.campus_navigation_backend.application.routing.helper.ResolvedRouteRequest;
import com.example.campus_navigation_backend.application.routing.helper.RouteRequestResolver;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.BuildingPointRow;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteRequestResolverTest {

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private FakeNavigationRepository navigationRepository;
    private BuildingPointStore buildingPointStore;
    private RouteRequestResolver resolver;

    @BeforeEach
    void setUp() {
        navigationRepository = new FakeNavigationRepository();
        buildingPointStore = new BuildingPointStore();
        buildingPointStore.initialize(Map.of(
                "Engineering Building", List.of(
                        new Point3D(100.0, 200.0, 10.0),
                        new Point3D(110.0, 210.0, 12.0)
                )
        ));
        resolver = new RouteRequestResolver(navigationRepository, buildingPointStore);
    }

    @Test
    void resolvesCoordinateStartAndCoordinateDestinationToMetricPoints() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.coordinate(127.0, 37.0, 5.0),
                RouteEndpointRequest.coordinate(128.0, 38.0, null)
        );

        ResolvedRouteRequest resolved = resolver.resolve(request);

        assertThat(resolved.start().type()).isEqualTo(RouteEndpointType.COORDINATE);
        assertThat(resolved.start().points()).containsExactly(new Point3D(127000.0, 37000.0, 5.0));
        assertThat(resolved.start().displayName()).isNull();
        assertThat(resolved.destination().type()).isEqualTo(RouteEndpointType.COORDINATE);
        assertThat(resolved.destination().points()).containsExactly(new Point3D(128000.0, 38000.0, 0.0));
        assertThat(navigationRepository.transformCallCount).isEqualTo(2);
    }

    @Test
    void resolvesBuildingStartAndBuildingDestinationFromStore() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.building("engineering building"),
                RouteEndpointRequest.building("Engineering Building")
        );

        ResolvedRouteRequest resolved = resolver.resolve(request);

        assertThat(resolved.start().type()).isEqualTo(RouteEndpointType.BUILDING);
        assertThat(resolved.start().displayName()).isEqualTo("engineering building");
        assertThat(resolved.start().points()).containsExactly(
                new Point3D(100.0, 200.0, 10.0),
                new Point3D(110.0, 210.0, 12.0)
        );
        assertThat(resolved.destination().type()).isEqualTo(RouteEndpointType.BUILDING);
        assertThat(resolved.destination().displayName()).isEqualTo("Engineering Building");
        assertThat(resolved.destination().points()).hasSize(2);
        assertThat(navigationRepository.transformCallCount).isZero();
    }

    @Test
    void rejectsMissingEndpoint() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                null,
                RouteEndpointRequest.coordinate(128.0, 38.0, 0.0)
        );

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start endpoint is required");
    }

    @Test
    void rejectsCoordinateEndpointWithoutLat() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.coordinate(127.0, null, 0.0),
                RouteEndpointRequest.coordinate(128.0, 38.0, 0.0)
        );

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lon and lat are required");
    }

    @Test
    void rejectsUnknownBuildingName() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.coordinate(127.0, 37.0, 0.0),
                RouteEndpointRequest.building("Unknown Building")
        );

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No building point found");
    }

    private static class FakeNavigationRepository implements NavigationRepository {
        private int transformCallCount;

        @Override
        public List<GraphNodeRow> findAllGraphNodes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<GraphEdgeRow> findAllGraphEdges() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<BuildingPointRow> findAllBuildingPoints() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TransformedPointRow transformToMetric(double lon, double lat, double ele) {
            transformCallCount++;
            return new TransformedPointRow(lon * 1000.0, lat * 1000.0, ele);
        }

        @Override
        public List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints) {
            throw new UnsupportedOperationException();
        }
    }
}


