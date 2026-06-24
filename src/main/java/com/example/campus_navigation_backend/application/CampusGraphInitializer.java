package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.GradeAdjustedEdgeCostPolicy;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CampusGraphInitializer {

    private final NavigationRepository navigationRepository;
    private final GradeAdjustedEdgeCostPolicy edgeCostPolicy;
    private final CampusGraphStore campusGraphStore;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        long start = System.currentTimeMillis();

        CampusGraph.Builder builder = CampusGraph.builder();

        long nodeStart = System.currentTimeMillis();
        loadNodes(builder);
        long nodeEnd = System.currentTimeMillis();

        long edgeStart = System.currentTimeMillis();
        loadEdges(builder);
        long edgeEnd = System.currentTimeMillis();

        campusGraphStore.initialize(builder.build());

        long end = System.currentTimeMillis();

        System.out.println("loadNodes ms = " + (nodeEnd - nodeStart));
        System.out.println("loadEdges ms = " + (edgeEnd - edgeStart));
        System.out.println("graph init total ms = " + (end - start));
    }

    private void loadNodes(CampusGraph.Builder builder) {
        List<GraphNodeRow> nodes = navigationRepository.findAllGraphNodes();

        for (GraphNodeRow row : nodes) {
            builder.addNode(row.id(), row.graphNodeType(), row.id(), row.point());
            if (row.description() != null && !row.description().isBlank()) {
                builder.addBuildingEntrance(normalize(row.description()), row.id());
            }
        }
    }

    private void loadEdges(CampusGraph.Builder builder) {
        List<GraphEdgeRow> edges = navigationRepository.findAllGraphEdges();

        for (GraphEdgeRow row : edges) {
            GraphNode source = builder.getNode(row.source());
            GraphNode target = builder.getNode(row.target());
            if (source == null || target == null) {
                throw new IllegalStateException("Edge endpoint node not found. edgeId=" + row.id());
            }

            double elevationDelta = target.point().z() - source.point().z();
            double forwardCost = edgeCostPolicy.calculate(row.highway(), row.highway(), row.cost(), elevationDelta);
            double reverseCost = edgeCostPolicy.calculate(row.highway(), row.highway(), row.cost(), -elevationDelta);

            builder.addDirectedEdge(source.id(), target.id(), forwardCost, row.highway(), row.geometry());
            builder.addDirectedEdge(target.id(), source.id(), reverseCost, row.highway(), reverse(row.geometry()));
        }
    }

    private List<Point3D> reverse(List<Point3D> geometry) {
        List<Point3D> copied = new java.util.ArrayList<>(geometry);
        Collections.reverse(copied);
        return copied;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

}
