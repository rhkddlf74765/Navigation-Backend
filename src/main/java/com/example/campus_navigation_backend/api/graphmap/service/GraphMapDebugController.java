package com.example.campus_navigation_backend.api.graphmap.service;

import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapPointRequest;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapProjectionResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteSessionResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteStartRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 그래프 지도 화면에서 점 투영과 라우팅 디버그 polling을 제공하는 에이피아이이다.
 */
@RestController
@RequestMapping("/api/navigation/graph-map")
@RequiredArgsConstructor
public class GraphMapDebugController {

    private final GraphMapDebugService graphMapDebugService;

    @PostMapping("/projections")
    public GraphMapProjectionResponse project(@RequestBody GraphMapPointRequest request) {
        return graphMapDebugService.project(request);
    }

    @PostMapping("/routes")
    public GraphMapRouteSessionResponse startRoute(@RequestBody GraphMapRouteStartRequest request) {
        return graphMapDebugService.startRoute(request);
    }

    @GetMapping("/routes/{sessionId}")
    public GraphMapRouteSessionResponse getRouteProgress(@PathVariable UUID sessionId) {
        return graphMapDebugService.getSession(sessionId);
    }
}
