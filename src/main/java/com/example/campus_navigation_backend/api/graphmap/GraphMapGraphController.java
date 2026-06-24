package com.example.campus_navigation_backend.api.graphmap;

import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGraphResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Graph snapshot API for the viewer page.
 */
@RestController
@RequestMapping("/api/navigation/graph-map")
@RequiredArgsConstructor
public class GraphMapGraphController {

    private final GraphMapVisualizationService graphMapVisualizationService;

    @GetMapping("/graph")
    public GraphMapGraphResponse graph() {
        return graphMapVisualizationService.loadGraph();
    }
}
