package com.example.campus_navigation_backend.api.navigation;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
import com.example.campus_navigation_backend.visualizer.GraphMapFacade;
import com.example.campus_navigation_backend.visualizer.GraphMapResponse;
import com.example.campus_navigation_backend.visualizer.ProjectionMapRequest;
import com.example.campus_navigation_backend.visualizer.ProjectionMapResponse;
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
 * REST controller that delegates production routing APIs and development map APIs to the application layer.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NavigationController {

    private final CampusNavigationFacade campusNavigationFacade;
    private final RouteMapFacade routeMapFacade;
    private final GraphMapFacade graphMapFacade;

    /**
     * Finds the shortest route for the requested start and destination endpoints.
     *
     * @param request route search request
     * @return shortest route response
     */
    @PostMapping("/routes")
    public ResponseEntity<RouteResponse> findRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = campusNavigationFacade.findRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Checks whether the server is running.
     *
     * @return server status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("navigation server is running");
    }

    /**
     * Returns a WGS84 route for the development map page.
     *
     * @param request route search request
     * @return map rendering route response
     */
    @PostMapping("/test/route-map/routes")
    public ResponseEntity<RouteMapResponse> findRouteForMap(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeMapFacade.findRouteForMap(request));
    }

    /**
     * Returns the initialized graph in coordinates used by the development map page.
     *
     * @return map rendering graph response
     */
    @GetMapping("/test/graph-map")
    public ResponseEntity<GraphMapResponse> getGraphForMap() {
        return ResponseEntity.ok(graphMapFacade.getGraphForMap());
    }

    /**
     * Projects a selected map point onto the nearest graph edge.
     *
     * @param request projection coordinate request
     * @return map rendering projection result
     */
    @PostMapping("/test/graph-map/projections")
    public ResponseEntity<ProjectionMapResponse> projectPointForMap(@Valid @RequestBody ProjectionMapRequest request) {
        return ResponseEntity.ok(graphMapFacade.projectPointForMap(request));
    }
}
