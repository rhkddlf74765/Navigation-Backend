package com.example.campus_navigation_backend.api.navigation;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
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
}
