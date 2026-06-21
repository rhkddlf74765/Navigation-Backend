package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.navigation.DestPoint;
import com.example.campus_navigation_backend.domain.navigation.StartPoint;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 경로 탐색 유스케이스를 구성하는 application facade이다.
 * 좌표 변환, 목적지 출입구 조회, 시작 후보 선정, 응답 조립을 순서대로 위임한다.
 */
@Service
@RequiredArgsConstructor
public class CampusNavigationFacade {

    private final NavigationRepository navigationRepository;
    private final CampusGraphStore campusGraphStore;
    private final StartCandidateFinder startCandidateFinder;
    private final RouteResponseAssembler routeResponseAssembler;

    /**
     * 사용자의 현재 위치와 목적지 건물명을 기준으로 최단 경로 응답을 생성한다.
     *
     * @param request 경도, 위도, 고도, 목적지 건물명을 포함한 요청
     * @return 최종 경로 응답
     */
    public RouteResponse findRoute(RouteRequest request) {
        StartPoint startPoint = StartPoint.currentLocation(transformCurrentPoint(request));
        DestPoint destPoint = new DestPoint(request.destinationBuildingName());

        List<Long> targetEntranceNodeIds =
                campusGraphStore.findEntranceNodeIdsByBuildingName(destPoint.buildingName());
        if (targetEntranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException("No entrance found for destination building.");
        }

        List<StartCandidateFinder.StartCandidate> candidates =
                startCandidateFinder.findCandidates(startPoint.currentLocation());

        return routeResponseAssembler.assembleBestRoute(
                destPoint.buildingName(),
                startPoint.currentLocation(),
                candidates,
                targetEntranceNodeIds
        );
    }

    /**
     * 요청의 WGS84 좌표를 경로 계산에 사용하는 metric 좌표로 변환한다.
     *
     * @param request 경로 탐색 요청
     * @return metric 좌표계의 현재 위치
     */
    private Point3D transformCurrentPoint(RouteRequest request) {
        TransformedPointRow transformed = navigationRepository.transformToMetric(
                request.longitude(),
                request.latitude(),
                request.altitude() == null ? 0.0 : request.altitude()
        );

        return transformed.toPoint3D();
    }
}
