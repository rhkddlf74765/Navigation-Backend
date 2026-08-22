package com.example.campus_navigation_backend.domain.geo;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;

public interface CoordinateTransformer {

    MetricPoint toMetric(
            GeoPoint point
    );

    GeoPoint toWgs84(
            MetricPoint point
    );
}
