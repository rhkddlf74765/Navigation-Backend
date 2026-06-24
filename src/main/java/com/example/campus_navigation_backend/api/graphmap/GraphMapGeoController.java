package com.example.campus_navigation_backend.api.graphmap;

import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGeoGraphResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGeoPointRequest;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGeoProjectionResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGeoRouteStartRequest;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Geo-based graph map API for Leaflet rendering.
 */
@RestController
@RequestMapping("/api/navigation/graph-map/geo")
@RequiredArgsConstructor
public class GraphMapGeoController {

    private final GraphMapGeoService graphMapGeoService;

    @GetMapping("/graph")
    public GraphMapGeoGraphResponse graph() {
        return graphMapGeoService.loadGraph();
    }

    @PostMapping("/projections")
    public GraphMapGeoProjectionResponse project(@RequestBody GraphMapGeoPointRequest request) {
        return graphMapGeoService.project(request);
    }

    @PostMapping("/routes")
    public GraphMapRouteSessionResponse startRoute(@RequestBody GraphMapGeoRouteStartRequest request) {
        return graphMapGeoService.startRoute(request);
    }

    @GetMapping("/routes/{sessionId}")
    public GraphMapRouteSessionResponse route(@PathVariable UUID sessionId) {
        return graphMapGeoService.getSession(sessionId);
    }
}
