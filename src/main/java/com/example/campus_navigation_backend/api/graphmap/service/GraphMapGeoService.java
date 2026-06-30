package com.example.campus_navigation_backend.api.graphmap.service;

import com.example.campus_navigation_backend.api.graphmap.dto.*;
import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
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
 * 그래프 지도 화면에 필요한 지리 좌표 기반 그래프 및 라우팅 데이터를 제공한다.
 * <p>
 * 응용 라우팅 facade의 결과를 그래프 지도 DTO로 변환하고, WGS84 지도 좌표와 핵심 그래프가 사용하는
 * 미터 단위 좌표 사이의 변환을 담당한다.
 */
@Service
@RequiredArgsConstructor
public class GraphMapGeoService {

    private final NavigationRepository navigationRepository;
    private final CampusGraphStore campusGraphStore;
    private final GraphMapDebugService graphMapDebugService;
    private final CampusNavigationFacade campusNavigationFacade;
    private final Map<UUID, GraphMapRouteSessionResponse> routeSessions = new ConcurrentHashMap<>();

    /**
     * 지도 UI는 내부 metric 그래프 좌표가 아니라 지리 좌표를 렌더링하므로,
     * 그래프 노드와 엣지를 WGS84 형태로 조회한다.
     */
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

    /**
     * 지도 좌표를 먼저 metric 좌표로 변환해 그래프에 투영한 뒤,
     * 화면 표시를 위해 투영점을 다시 WGS84로 변환한다.
     */
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

    /**
     * 시각화 화면도 일반 API 라우팅과 동일한 동작을 사용하도록 기본 application facade를 통해 graph-map 경로 계산을 시작한다.
     */
    public GraphMapRouteSessionResponse startRoute(GraphMapGeoRouteStartRequest request) {
        RouteResponse routeResponse = campusNavigationFacade.findRoute(
                new RouteRequest(
                        RouteEndpointRequest.coordinate(
                                request.startPoint().longitude(),
                                request.startPoint().latitude(),
                                request.startPoint().altitude()
                        ),
                        RouteEndpointRequest.coordinate(
                                request.destinationPoint().longitude(),
                                request.destinationPoint().latitude(),
                                request.destinationPoint().altitude()
                        )
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

    /**
     * 이 서비스에 저장된 완료된 graph-map 경로 세션을 반환하고,
     * 화면에서 이전 디버그 세션을 요청할 수 있으므로 없을 경우 기존 디버그 서비스로 대체 조회한다.
     */
    public GraphMapRouteSessionResponse getSession(java.util.UUID sessionId) {
        GraphMapRouteSessionResponse session = routeSessions.get(sessionId);
        if (session != null) {
            return session;
        }
        return graphMapDebugService.getSession(sessionId);
    }

    /**
     * 지도 polyline 렌더링을 위해 application 경로의 metric DTO 좌표를 WGS84 좌표로 변환한다.
     */
    private List<GraphMapGeoPointResponse> toGeoPath(List<RoutePoint> routePoints) {
        List<Point3D> metricPoints = routePoints.stream()
                .map(point -> new Point3D(point.x(), point.y(), point.z()))
                .toList();
        return navigationRepository.transformMetricPointsToWgs84(metricPoints).stream()
                .map(row -> new GraphMapGeoPointResponse(row.longitude(), row.latitude(), row.altitude()))
                .toList();
    }
}
