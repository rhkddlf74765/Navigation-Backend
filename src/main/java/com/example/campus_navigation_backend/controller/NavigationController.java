package com.example.campus_navigation_backend.controller;

import com.example.campus_navigation_backend.application.CampusNavigationFacade;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.visualizer.RouteMapFacade;
import com.example.campus_navigation_backend.visualizer.RouteMapResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/navigation")
@RequiredArgsConstructor
public class NavigationController {

    private final CampusNavigationFacade campusNavigationFacade;

    /*
        visualizer를 위한 필드 추가
     */
    private final RouteMapFacade routeMapFacade;

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> findRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = campusNavigationFacade.findRoute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("navigation server is running");
    }

    /*
        visualizer를 위한 메서드 추가
     */
    @PostMapping("/route-map")
    public ResponseEntity<RouteMapResponse> findRouteForMap(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeMapFacade.findRouteForMap(request));
    }
}
