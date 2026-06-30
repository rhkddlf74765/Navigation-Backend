package com.example.campus_navigation_backend.api.graphmap;

import com.example.campus_navigation_backend.api.graphmap.dto.*;
import com.example.campus_navigation_backend.application.CampusNavigationFacade;
import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Geographic facade for the graph-map screen.
 */
@Service
@RequiredArgsConstructor
public class GraphMapGeoService {

    private final NavigationRepository navigationRepository;
    private final CampusGraphStore campusGraphStore;
    private final GraphMapDebugService graphMapDebugService;
    private final CampusNavigationFacade campusNavigationFacade;
    private final Map<UUID, GraphMapRouteSessionResponse> routeSessions = new ConcurrentHashMap<>();

    public GraphMapGeoGraphResponse loadGraph() {
        List<GraphNode> nodes = campusGraphStore.getNodes();
        List<Point3D> nodePoints = nodes.stream().map(GraphNode::point).toList();
        List<Wgs84PointRow> transformedNodes = navigationRepository.transformMetricPointsToWgs84(nodePoints);

        Map<Long, GraphMapGeoNodeResponse> nodeResponses = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode node = nodes.get(i);
            Wgs84PointRow transformed = transformedNodes.get(i);
            nodeResponses.put(node.id(), new GraphMapGeoNodeResponse(
                    node.id(),
                    node.type().name(),
                    null,
                    transformed.longitude(),
                    transformed.latitude(),
                    transformed.altitude()
            ));
        }

        List<GraphMapGeoEdgeResponse> edgeResponses = new ArrayList<>();
        for (GraphEdge edge : campusGraphStore.getEdges()) {
            List<Wgs84PointRow> transformedGeometry = navigationRepository.transformMetricPointsToWgs84(edge.geometry());
            edgeResponses.add(new GraphMapGeoEdgeResponse(
                    edge.fromNodeId(),
                    edge.toNodeId(),
                    edge.edgeType(),
                    edge.cost(),
                    transformedGeometry.stream()
                            .map(row -> new GraphMapGeoPointResponse(row.longitude(), row.latitude(), row.altitude()))
                            .toList()
            ));
        }

        return new GraphMapGeoGraphResponse(List.copyOf(nodeResponses.values()), List.copyOf(edgeResponses));
    }

    public GraphMapGeoProjectionResponse project(GraphMapGeoPointRequest request) {
        Point3D metricPoint = navigationRepository.transformToMetric(
                request.longitude(),
                request.latitude(),
                request.altitude() == null ? 0.0 : request.altitude()
        ).toPoint3D();

        GraphMapProjectionResponse metricProjection = graphMapDebugService.project(
                new GraphMapPointRequest(metricPoint.x(), metricPoint.y(), metricPoint.z())
        );

        GraphMapPointResponse projectedMetricPoint = metricProjection.projectedPoint();
        List<Wgs84PointRow> transformed = navigationRepository.transformMetricPointsToWgs84(
                List.of(new Point3D(projectedMetricPoint.x(), projectedMetricPoint.y(), projectedMetricPoint.z()))
        );
        Wgs84PointRow projected = transformed.get(0);

        return new GraphMapGeoProjectionResponse(
                request,
                new GraphMapGeoPointResponse(projected.longitude(), projected.latitude(), projected.altitude()),
                metricProjection.edgeFromNodeId(),
                metricProjection.edgeToNodeId(),
                metricProjection.distanceToEdge(),
                metricProjection.accessCostToFromNode(),
                metricProjection.accessCostToToNode()
        );
    }

    public GraphMapRouteSessionResponse startRoute(GraphMapGeoRouteStartRequest request) {
        RouteResponse routeResponse = campusNavigationFacade.findRoute(
                new RouteRequest(
                        request.startPoint().longitude(),
                        request.startPoint().latitude(),
                        request.startPoint().altitude(),
                        null,
                        request.destinationPoint().longitude(),
                        request.destinationPoint().latitude(),
                        request.destinationPoint().altitude()
                )
        );

        List<GraphMapGeoPointResponse> path = toGeoPath(routeResponse.path());
        GraphMapRouteResultResponse result = new GraphMapRouteResultResponse(
                true,
                routeResponse.totalDistanceMeters(),
                path
        );

        UUID sessionId = UUID.randomUUID();
        GraphMapRouteSessionResponse session = new GraphMapRouteSessionResponse(
                sessionId,
                GraphMapRouteSearchStatus.COMPLETED,
                "Route search completed.",
                result
        );
        routeSessions.put(sessionId, session);
        return session;
    }

    public GraphMapRouteSessionResponse getSession(java.util.UUID sessionId) {
        GraphMapRouteSessionResponse session = routeSessions.get(sessionId);
        if (session != null) {
            return session;
        }
        return graphMapDebugService.getSession(sessionId);
    }

    private List<GraphMapGeoPointResponse> toGeoPath(List<RoutePoint> routePoints) {
        List<Point3D> metricPoints = routePoints.stream()
                .map(point -> new Point3D(point.x(), point.y(), point.z()))
                .toList();
        return navigationRepository.transformMetricPointsToWgs84(metricPoints).stream()
                .map(row -> new GraphMapGeoPointResponse(row.longitude(), row.latitude(), row.altitude()))
                .toList();
    }
}
