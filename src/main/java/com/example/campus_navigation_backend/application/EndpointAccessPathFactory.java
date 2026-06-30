package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.projection.PointProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * projection된 엣지 후보를 A* 탐색 endpoint 후보로 변환한다.
 * <p>
 * 출발 측은 "출발 좌표 -> projection 점 -> endpoint" 방향으로, 도착 측은
 * "endpoint -> projection 점 -> 도착 좌표" 방향으로 접근 경로를 생성한다.
 */
@Component
@RequiredArgsConstructor
public class EndpointAccessPathFactory {

    private final RouteSegmentBuilder routeSegmentBuilder;

    /**
     * 출발 좌표에서 각 projection 엣지의 양 끝 노드까지 도달하는 접근 경로 후보를 만든다.
     *
     * @param startPoint 원본 출발 좌표
     * @param projectedEdges 출발 좌표 주변의 projection 엣지 후보
     * @return 출발 측 A* 시작 endpoint 후보 목록
     */
    public List<EndpointAccessPath> createStartAccessPaths(Point3D startPoint,
                                                           List<ProjectedEdgeCandidate> projectedEdges) {
        List<EndpointAccessPath> paths = new ArrayList<>();

        for (ProjectedEdgeCandidate candidate : projectedEdges) {
            PointProjection projection = candidate.projection();
            GraphEdge edge = candidate.edge();

            RouteSegment connector = routeSegmentBuilder.connector(startPoint, projection.projectedPoint());
            RouteSegment toFromNode = routeSegmentBuilder.projectionToFromNode(projection);
            RouteSegment toToNode = routeSegmentBuilder.projectionToToNode(projection);

            paths.add(new EndpointAccessPath(
                    edge.fromNodeId(),
                    new RoutePath(connector.cost(), connector.path())
                            .append(new RoutePath(toFromNode.cost(), toFromNode.path()))
            ));
            paths.add(new EndpointAccessPath(
                    edge.toNodeId(),
                    new RoutePath(connector.cost(), connector.path())
                            .append(new RoutePath(toToNode.cost(), toToNode.path()))
            ));
        }

        return paths;
    }

    /**
     * 각 projection 엣지의 양 끝 노드에서 도착 좌표까지 도달하는 접근 경로 후보를 만든다.
     *
     * @param destinationPoint 원본 도착 좌표
     * @param projectedEdges 도착 좌표 주변의 projection 엣지 후보
     * @return 도착 측 A* 도착 endpoint 후보 목록
     */
    public List<EndpointAccessPath> createDestinationAccessPaths(Point3D destinationPoint,
                                                                 List<ProjectedEdgeCandidate> projectedEdges) {
        List<EndpointAccessPath> paths = new ArrayList<>();

        for (ProjectedEdgeCandidate candidate : projectedEdges) {
            PointProjection projection = candidate.projection();
            GraphEdge edge = candidate.edge();

            RouteSegment fromNodeToProjection = routeSegmentBuilder.fromNodeToProjection(projection);
            RouteSegment toNodeToProjection = routeSegmentBuilder.toNodeToProjection(projection);
            RouteSegment connector = routeSegmentBuilder.connector(projection.projectedPoint(), destinationPoint);

            paths.add(new EndpointAccessPath(
                    edge.fromNodeId(),
                    new RoutePath(fromNodeToProjection.cost(), fromNodeToProjection.path())
                            .append(new RoutePath(connector.cost(), connector.path()))
            ));
            paths.add(new EndpointAccessPath(
                    edge.toNodeId(),
                    new RoutePath(toNodeToProjection.cost(), toNodeToProjection.path())
                            .append(new RoutePath(connector.cost(), connector.path()))
            ));
        }

        return paths;
    }
}
