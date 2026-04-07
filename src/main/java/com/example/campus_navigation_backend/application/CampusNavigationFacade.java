package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 비즈니스 흐름만 조합하고, 직접 SQL이나 A* 로직을 넣지는 않는다.
 * <p>Orchestrator 역할을 수행한다.</p>
 */
@Service
@RequiredArgsConstructor
public class CampusNavigationFacade {
    /**
     * 현재 위치 좌표 변환 요청
     * 시작 후보 노드 조회
     * 건물명으로 entrance node 목록 조회
     * A* 여러 번 실행
     * 최적 경로 생성
     * 응답 생성
     */

    private final NavigationRepository navigationRepository;
    private final GraphInitializationService graphInitializationService;
    private final StartCandidateFinder startCandidateFinder;
    private final AStarPathFinder aStarPathFinder;
    private final RouteResponseAssembler routeResponseAssembler;

    public RouteResponse findRoute(RouteRequest request) {
        CampusGraph graph = graphInitializationService.getGraph();

        TransformedPointRow transformed = navigationRepository.transformToMetric(
                request.longitude(),
                request.latitude(),
                request.altitude() == null ? 0.0 : request.altitude()
        );
        Point3D currentPoint = transformed.toPoint3D();

        List<Long> targetEntranceNodeIds = graph.findEntranceNodeIdsByBuildingName(request.destinationBuildingName());
        if (targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("해당 건물의 입구를 찾을 수 없습니다.");
        }

        List<StartCandidateFinder.StartCandidate> candidates =
                startCandidateFinder.findCandidates(graph, currentPoint);

        return routeResponseAssembler.assembleBestRoute(
                request.destinationBuildingName(),
                currentPoint,
                graph,
                candidates,
                targetEntranceNodeIds,
                aStarPathFinder
        );
    }
}
