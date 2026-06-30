package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 해석된 라우팅 endpoint에 대해 출발 측과 도착 측의 모든 접근 경로를 생성한다.
 * <p>
 * 각 endpoint 후보 지점 주변의 인접 엣지를 찾고, {@link EndpointAccessPathFactory}에 위임해 실제 접근 경로로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class EndpointAccessPathResolver {

    private final NavigationProperties properties;
    private final NearbyEdgeFinder nearbyEdgeFinder;
    private final EndpointAccessPathFactory endpointAccessPathFactory;

    /**
     * 그래프 탐색은 그래프 노드 ID 사이에서만 수행되므로, 임의 좌표나 건물 지점에서 시작하거나 종료할 수 있게 접근 경로 후보를 생성한다.
     */
    public EndpointAccessPaths resolve(ResolvedRouteRequest request) {
        int edgeLimit = Math.max(1, properties.maxCandidates());

        return new EndpointAccessPaths(
                createStartAccessPaths(request.start().points(), edgeLimit),
                createDestinationAccessPaths(request.destination().points(), edgeLimit)
        );
    }

    /**
     * 모든 출발 지점을 인접 엣지에 투영하고 출발 좌표에서 그래프 endpoint로 향하는 경로를 만든다.
     */
    private List<EndpointAccessPath> createStartAccessPaths(List<Point3D> startPoints, int edgeLimit) {
        return startPoints.stream()
                .flatMap(startPoint -> {
                    List<ProjectedEdgeCandidate> edges = nearbyEdgeFinder.findNearbyEdges(startPoint, edgeLimit);
                    return endpointAccessPathFactory.createStartAccessPaths(startPoint, edges).stream();
                })
                .toList();
    }

    /**
     * 모든 도착 지점을 인접 엣지에 투영하고 그래프 endpoint에서 도착 좌표로 향하는 경로를 만든다.
     */
    private List<EndpointAccessPath> createDestinationAccessPaths(List<Point3D> destinationPoints, int edgeLimit) {
        return destinationPoints.stream()
                .flatMap(destinationPoint -> {
                    List<ProjectedEdgeCandidate> edges = nearbyEdgeFinder.findNearbyEdges(destinationPoint, edgeLimit);
                    return endpointAccessPathFactory.createDestinationAccessPaths(destinationPoint, edges).stream();
                })
                .toList();
    }
}
