package com.example.campus_navigation_backend.domain.graph;

import java.util.List;

public interface RoutingArc {

    long fromNodeId();

    long toNodeId();

    double distanceMeters();

    double cost();

    List<MetricPoint> geometry();

    RoutingArcKind kind();
}
