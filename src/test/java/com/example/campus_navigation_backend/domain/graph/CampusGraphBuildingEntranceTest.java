package com.example.campus_navigation_backend.domain.graph;

import com.example.campus_navigation_backend.application.routing.ResolvedRouteEndpoint;
import com.example.campus_navigation_backend.application.routing.ResolvedRouteRequest;
import com.example.campus_navigation_backend.application.routing.RoutingOverlayFactory;
import com.example.campus_navigation_backend.domain.routing.RoutingOverlay;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CampusGraphBuildingEntranceTest {

    @Test
    void keepsMultipleEntrancesByBuildingId() {
        CampusGraph.Builder builder =
                CampusGraph.builder();

        builder.addNode(
                10L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(0.0, 0.0, 0.0)
        );
        builder.addNode(
                11L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(1.0, 0.0, 0.0)
        );
        builder.addBuildingEntrance(
                7L,
                "Engineering Building",
                10L
        );
        builder.addBuildingEntrance(
                7L,
                "Engineering Building",
                11L
        );

        CampusGraph graph =
                builder.build();

        assertThat(
                graph.findEntranceNodeIdsByBuildingId(
                        7L
                )
        ).containsExactly(
                10L,
                11L
        );

        assertThat(
                graph.findEntranceNodeIdsByBuildingName(
                        " engineering   building "
                )
        ).containsExactly(
                10L,
                11L
        );
    }

    @Test
    void overlayConnectsAllBuildingEntrances() {
        CampusGraphStore store =
                new CampusGraphStore();

        CampusGraph.Builder builder =
                CampusGraph.builder();

        builder.addNode(
                10L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(0.0, 0.0, 0.0)
        );
        builder.addNode(
                11L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(1.0, 0.0, 0.0)
        );
        builder.addNode(
                20L,
                GraphNodeType.ENTRANCE,
                new MetricPoint(10.0, 0.0, 0.0)
        );
        builder.addBuildingEntrance(
                1L,
                "Start",
                10L
        );
        builder.addBuildingEntrance(
                1L,
                "Start",
                11L
        );
        builder.addBuildingEntrance(
                2L,
                "Destination",
                20L
        );

        store.initialize(
                builder.build()
        );

        RoutingOverlayFactory factory =
                new RoutingOverlayFactory(
                        store
                );

        RoutingOverlay overlay =
                factory.create(
                        new ResolvedRouteRequest(
                                new ResolvedRouteEndpoint.Building(
                                        "Start",
                                        store.findEntranceNodeIdsByBuildingId(
                                                1L
                                        )
                                ),
                                new ResolvedRouteEndpoint.Building(
                                        "Destination",
                                        store.findEntranceNodeIdsByBuildingId(
                                                2L
                                        )
                                )
                        )
                );

        assertThat(
                overlay.getAdjacency(
                        overlay.startNodeId()
                )
        ).extracting(
                arc -> arc.toNodeId()
        ).containsExactlyInAnyOrder(
                10L,
                11L
        );

        assertThat(
                overlay.getAdjacency(20L)
        ).anySatisfy(
                arc ->
                        assertThat(arc.toNodeId())
                                .isEqualTo(
                                        overlay.goalNodeId()
                                )
        );
    }
}
