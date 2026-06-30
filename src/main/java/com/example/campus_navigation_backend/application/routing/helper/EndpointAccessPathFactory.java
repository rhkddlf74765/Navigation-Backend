package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.projection.PointProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 투영된 인접 엣지 후보를 실제 A* endpoint 후보 접근 경로로 변환한다.
 * <p>
 * 출발 접근 경로는 출발 좌표에서 투영점을 지나 endpoint 노드로 향하고,
 * 도착 접근 경로는 endpoint 노드에서 투영점을 지나 도착 좌표로 향한다.
 */
@Component
@RequiredArgsConstructor
public class EndpointAccessPathFactory {

    private final RouteSegmentBuilder routeSegmentBuilder;

    /**
     * 평가기가 각 투영 엣지의 양 끝 endpoint를 모두 시도할 수 있도록 가능한 모든 출발 측 접근 경로를 생성한다.
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
     * 평가기가 각 투영 엣지의 양 끝 endpoint를 모두 시도할 수 있도록 가능한 모든 도착 측 접근 경로를 생성한다.
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
