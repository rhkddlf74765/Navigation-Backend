package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.CurrentProjectionRow;
import com.example.campus_navigation_backend.repository.dto.EntranceRow;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import com.example.campus_navigation_backend.repository.dto.LineSegmentRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;

import java.util.List;

public interface NavigationRepository {

    List<GraphNodeRow> findAllGraphNodes();

    List<GraphEdgeRow> findAllGraphEdges();

    List<LineSegmentRow> findAllWalkableLineSegments();

    List<EntranceRow> findAllEntrances();

    TransformedPointRow transformToMetric(double longitude, double latitude, double altitude);

    CurrentProjectionRow projectCurrentLocationToNearestLine(double x, double y, double z);

    List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints);
}
