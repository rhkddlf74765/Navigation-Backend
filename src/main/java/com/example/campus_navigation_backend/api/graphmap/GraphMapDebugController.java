package com.example.campus_navigation_backend.api.graphmap;

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
 * API for point projection and route-debug polling on the graph map screen.
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
