package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import com.example.campus_navigation_backend.domain.graph.cost.EdgeCostPolicy;
import com.example.campus_navigation_backend.repository.GraphDataRepository;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CampusGraphLoader {

    private final GraphDataRepository
            graphDataRepository;

    private final EdgeCostPolicy
            edgeCostPolicy;

    public CampusGraphLoader(
            GraphDataRepository graphDataRepository,
            EdgeCostPolicy edgeCostPolicy
    ) {
        this.graphDataRepository =
                graphDataRepository;

        this.edgeCostPolicy =
                edgeCostPolicy;
    }

    public CampusGraph load(
            long graphVersionId
    ) {

        CampusGraph.Builder builder =
                CampusGraph.builder();

        loadNodes(
                builder,
                graphVersionId
        );

        loadEdges(
                builder,
                graphVersionId
        );

        return builder.build();
    }

    private void loadNodes(
            CampusGraph.Builder builder,
            long graphVersionId
    ) {

        List<GraphNodeRow> rows =
                graphDataRepository
                        .findAllGraphNodes(
                                graphVersionId
                        );

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
                    || row.buildingName()
                    .isBlank()) {

                throw new IllegalStateException(
                        "Entrance graph node references a building without a name. graphNodeId="
                                + row.id()
                );
            }

            builder.addBuildingEntrance(
                    row.buildingId(),
                    row.buildingName(),
                    row.id()
            );
        }
    }

    private void loadEdges(
            CampusGraph.Builder builder,
            long graphVersionId
    ) {

        List<GraphEdgeRow> rows =
                graphDataRepository
                        .findAllGraphEdges(
                                graphVersionId
                        );

        for (GraphEdgeRow row : rows) {

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