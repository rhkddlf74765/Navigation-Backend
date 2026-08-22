package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.RoutingGraph;

public interface PathFinder {

    PathResult findPath(
            RoutingGraph graph,
            long startNodeId,
            long goalNodeId
    );
}