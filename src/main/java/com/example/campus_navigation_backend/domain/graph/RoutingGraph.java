package com.example.campus_navigation_backend.domain.graph;

import java.util.List;

public interface RoutingGraph {

    boolean containsNode(
            long nodeId
    );

    List<? extends RoutingArc>
    getAdjacency(
            long nodeId
    );

    double estimateMinimumCost(
            long fromNodeId,
            long goalNodeId
    );
}
