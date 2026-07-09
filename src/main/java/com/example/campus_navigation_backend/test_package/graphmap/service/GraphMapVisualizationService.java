package com.example.campus_navigation_backend.test_package.graphmap.service;

import com.example.campus_navigation_backend.test_package.graphmap.dto.GraphMapEdgeResponse;
import com.example.campus_navigation_backend.test_package.graphmap.dto.GraphMapGraphResponse;
import com.example.campus_navigation_backend.test_package.graphmap.dto.GraphMapNodeResponse;
import com.example.campus_navigation_backend.test_package.graphmap.dto.GraphMapPointResponse;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 그래프 지도 뷰어에서 사용할 그래프 스냅샷을 생성한다.
 */
@Service
@RequiredArgsConstructor
public class GraphMapVisualizationService {

    private final CampusGraphStore campusGraphStore;

    public GraphMapGraphResponse loadGraph() {
        Map<Long, GraphMapNodeResponse> nodes = new LinkedHashMap<>();
        List<GraphMapEdgeResponse> edges = new ArrayList<>();

        for (GraphNode node : campusGraphStore.getNodes()) {
            nodes.put(node.id(), new GraphMapNodeResponse(node.id(), node.point().lon(), node.point().lat(), node.point().ele()));
        }

        for (GraphEdge edge : campusGraphStore.getEdges()) {
            List<Point3D> geometry = edge.geometry();
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }

            edges.add(new GraphMapEdgeResponse(
                    edge.fromNodeId(),
                    edge.toNodeId(),
                    edge.edgeType(),
                    edge.cost(),
                    geometry.stream()
                            .map(point -> new GraphMapPointResponse(point.lon(), point.lat(), point.ele()))
                            .toList()
            ));
        }

        return new GraphMapGraphResponse(List.copyOf(nodes.values()), List.copyOf(edges));
    }
}
