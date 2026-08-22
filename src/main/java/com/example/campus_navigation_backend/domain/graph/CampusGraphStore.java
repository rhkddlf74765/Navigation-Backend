package com.example.campus_navigation_backend.domain.graph;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CampusGraphStore {

    private final AtomicReference<CampusGraph>
            graphReference =
            new AtomicReference<>();

    public void initialize(
            CampusGraph graph
    ) {
        if (graph == null) {
            throw new IllegalArgumentException(
                    "CampusGraph must not be null."
            );
        }

        if (!graphReference.compareAndSet(
                null,
                graph
        )) {
            throw new IllegalStateException(
                    "CampusGraph has already been initialized."
            );
        }
    }

    public CampusGraph graph() {
        CampusGraph graph =
                graphReference.get();

        if (graph == null) {
            throw new IllegalStateException(
                    "CampusGraph has not been initialized yet."
            );
        }

        return graph;
    }

    public GraphNode getNode(
            long nodeId
    ) {
        GraphNode node =
                graph().getNode(nodeId);

        if (node == null) {
            throw new IllegalArgumentException(
                    "Graph node not found. nodeId="
                            + nodeId
            );
        }

        return node;
    }

    public List<GraphEdge>
    getAdjacency(
            long nodeId
    ) {
        return graph()
                .getBaseAdjacency(
                        nodeId
                );
    }

    public List<GraphNode> getNodes() {
        return graph().getNodes();
    }

    public List<GraphEdge> getEdges() {
        return graph().getEdges();
    }

    public List<PhysicalEdge>
    getPhysicalEdges() {
        return graph()
                .getPhysicalEdges();
    }

    public PhysicalEdge getPhysicalEdge(
            long edgeId
    ) {
        PhysicalEdge edge =
                graph()
                        .getPhysicalEdge(
                                edgeId
                        );

        if (edge == null) {
            throw new IllegalArgumentException(
                    "Physical edge not found."
            );
        }

        return edge;
    }

    public List<Long>
    findEntranceNodeIdsByBuildingName(
            String buildingName
    ) {
        return graph()
                .findEntranceNodeIdsByBuildingName(
                        buildingName
                );
    }

    public List<String>
    findBuildingNames() {
        return graph()
                .findBuildingNames();
    }
}