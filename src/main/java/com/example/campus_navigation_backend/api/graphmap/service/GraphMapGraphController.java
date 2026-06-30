package com.example.campus_navigation_backend.api.graphmap.service;

import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapGraphResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뷰어 페이지를 위한 그래프 스냅샷 에이피아이이다.
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
