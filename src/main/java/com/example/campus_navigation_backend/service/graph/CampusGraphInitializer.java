package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import com.example.campus_navigation_backend.domain.graph.cost.EdgeCostPolicy;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import com.example.campus_navigation_backend.repository.GraphDataRepository;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CampusGraphInitializer {

    private final GraphDataRepository
            graphDataRepository;

    private final NavigationProperties
            properties;

    private final EdgeCostPolicy
            edgeCostPolicy;

    private final CampusGraphStore
            campusGraphStore;

    private final EdgeSpatialIndex
            edgeSpatialIndex;

    public CampusGraphInitializer(
            GraphDataRepository
                    graphDataRepository,
            NavigationProperties properties,
            EdgeCostPolicy edgeCostPolicy,
            CampusGraphStore campusGraphStore,
            EdgeSpatialIndex edgeSpatialIndex
    ) {
        this.graphDataRepository =
                graphDataRepository;

        this.properties =
                properties;

        this.edgeCostPolicy =
                edgeCostPolicy;

        this.campusGraphStore =
                campusGraphStore;

        this.edgeSpatialIndex =
                edgeSpatialIndex;
    }

    @EventListener(
            ApplicationReadyEvent.class
    )
    public void initialize() {

        CampusGraph.Builder builder =
                CampusGraph.builder();

        loadNodes(builder);
        loadEdges(builder);

        CampusGraph graph =
                builder.build();

        campusGraphStore.initialize(
                graph
        );

        edgeSpatialIndex.initialize(
                graph.getPhysicalEdges()
        );
    }

    private void loadNodes(
            CampusGraph.Builder builder
    ) {
        List<GraphNodeRow> rows =
                graphDataRepository
                        .findAllGraphNodes();

        for (GraphNodeRow row : rows) {

            builder.addNode(
                    row.id(),
                    row.graphNodeType(),
                    row.point()
            );

            if (row.graphNodeType()
                    != GraphNodeType.ENTRANCE) {
                continue;
            }

            if (row.entranceId() == null) {
                throw new IllegalStateException(
                        "Entrance graph node has no source entrance. graphNodeId="
                                + row.id()
                );
            }

            if (row.buildingId() == null) {
                continue;
            }

            if (row.buildingName() == null
                    || row
                    .buildingName()
                    .isBlank()) {
                throw new IllegalStateException(
                        "Entrance graph node references a building without a name. graphNodeId="
                                + row.id()
                                + ", entranceId="
                                + row.entranceId()
                                + ", buildingId="
                                + row.buildingId()
                );
            }

            builder
                    .addBuildingEntrance(
                            row.buildingId(),
                            row.buildingName(),
                            row.id()
                    );
        }
    }

    private void loadEdges(
            CampusGraph.Builder builder
    ) {
        List<GraphEdgeRow> rows =
                graphDataRepository
                        .findAllGraphEdges();

        Set<String> walkable =
                properties.walkableHighways()
                        == null

                        ? Set.of()

                        : properties
                        .walkableHighways()
                        .stream()
                        .map(
                                value ->
                                        value
                                                .toLowerCase(
                                                        Locale.ROOT
                                                )
                        )
                        .collect(
                                Collectors
                                        .toUnmodifiableSet()
                        );

        for (GraphEdgeRow row : rows) {

            if (!walkable.isEmpty()
                    &&
                    (
                            row.highway() == null
                                    ||
                                    !walkable.contains(
                                            row.highway()
                                                    .toLowerCase(
                                                            Locale.ROOT
                                                    )
                                    )
                    )) {
                continue;
            }

            GraphNode source =
                    builder.getNode(
                            row.source()
                    );

            GraphNode target =
                    builder.getNode(
                            row.target()
                    );

            if (source == null
                    || target == null) {
                throw new IllegalStateException(
                        "Edge endpoint node not found. edgeId="
                                + row.id()
                );
            }

            PhysicalEdge edge =
                    PhysicalEdge.create(
                            row.id(),
                            row.source(),
                            row.target(),
                            row.distanceMeters(),
                            row.highway(),
                            row.geometry(),
                            edgeCostPolicy
                    );

            builder.addPhysicalEdge(
                    edge
            );
        }
    }
}
