package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import com.example.campus_navigation_backend.repository.GraphDataRepository;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampusGraphInitializerTest {

    @Test
    void failsFastWhenEntranceGraphNodeHasNoSourceEntrance() {
        CampusGraphInitializer initializer =
                initializer(
                        List.of(
                                new GraphNodeRow(
                                        4L,
                                        "entrance",
                                        "Legacy Building",
                                        null,
                                        10L,
                                        "Engineering Building",
                                        0.0,
                                        0.0,
                                        0.0
                                )
                        )
                );

        assertThatThrownBy(initializer::initialize)
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "Entrance graph node has no source entrance. graphNodeId=4"
                );
    }

    @Test
    void keepsUnlinkedEntranceGraphNodeButDoesNotRegisterBuildingCandidate() {
        CampusGraphStore store =
                new CampusGraphStore();

        CampusGraphInitializer initializer =
                initializer(
                        store,
                        List.of(
                                new GraphNodeRow(
                                        4L,
                                        "entrance",
                                        "Legacy Building",
                                        100L,
                                        null,
                                        null,
                                        0.0,
                                        0.0,
                                        0.0
                                )
                        )
                );

        initializer.initialize();

        assertThat(store.getNode(4L).type())
                .isEqualTo(
                        GraphNodeType.ENTRANCE
                );
        assertThat(
                store.findEntranceNodeIdsByBuildingId(
                        10L
                )
        ).isEmpty();
    }

    private CampusGraphInitializer initializer(
            List<GraphNodeRow> nodes
    ) {
        return initializer(
                new CampusGraphStore(),
                nodes
        );
    }

    private CampusGraphInitializer initializer(
            CampusGraphStore store,
            List<GraphNodeRow> nodes
    ) {
        NavigationProperties properties =
                new NavigationProperties(
                        4326,
                        5186,
                        5179,
                        30.0,
                        20,
                        25.0,
                        List.of("footway")
                );

        return new CampusGraphInitializer(
                new FakeGraphDataRepository(nodes),
                properties,
                (highway, distanceMeters, elevationDelta) ->
                        distanceMeters,
                store,
                new EdgeSpatialIndex(properties)
        );
    }

    private record FakeGraphDataRepository(
            List<GraphNodeRow> nodes
    ) implements GraphDataRepository {

        @Override
        public List<GraphNodeRow> findAllGraphNodes() {
            return nodes;
        }

        @Override
        public List<GraphEdgeRow> findAllGraphEdges() {
            return List.of();
        }
    }
}
