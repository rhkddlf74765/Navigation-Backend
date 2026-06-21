package com.example.campus_navigation_backend.controller;

import com.example.campus_navigation_backend.application.CampusNavigationFacade;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.visualizer.GraphMapFacade;
import com.example.campus_navigation_backend.visualizer.GraphMapResponse;
import com.example.campus_navigation_backend.visualizer.RouteMapFacade;
import com.example.campus_navigation_backend.visualizer.RouteMapResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Navigation API의 HTTP 요청을 받아 application layer로 전달하는 REST Controller이다.
 */
@RestController
@RequestMapping("/api/navigation")
@RequiredArgsConstructor
public class NavigationController {

    private final CampusNavigationFacade campusNavigationFacade;
    private final RouteMapFacade routeMapFacade;
    private final GraphMapFacade graphMapFacade;

    /**
     * 현재 위치와 목적지 건물명을 받아 내부 metric 좌표 기준의 최단 경로를 반환한다.
     *
     * @param request 경로 탐색 요청
     * @return 최단 경로 응답
     */
    @PostMapping("/route")
    public ResponseEntity<RouteResponse> findRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = campusNavigationFacade.findRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * navigation 서버의 실행 여부를 확인한다.
     *
     * @return 서버 상태 메시지
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("navigation server is running");
    }

    /**
     * 현재 위치와 목적지 건물명을 받아 지도 시각화에 사용할 WGS84 좌표 경로를 반환한다.
     *
     * @param request 경로 탐색 요청
     * @return 지도 표시용 경로 응답
     */
    @PostMapping("/route-map")
    public ResponseEntity<RouteMapResponse> findRouteForMap(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeMapFacade.findRouteForMap(request));
    }

    /**
     * 메모리에 초기화된 전체 그래프를 지도 시각화용 좌표로 반환한다.
     *
     * @return 지도 표시용 전체 그래프 응답
     */
    @GetMapping("/graph-map")
    public ResponseEntity<GraphMapResponse> getGraphForMap() {
        return ResponseEntity.ok(graphMapFacade.getGraphForMap());
    }
}
