package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.LineEndpoints;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.EntranceProjectionRow;
import com.example.campus_navigation_backend.repository.dto.LineSegmentRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 그래프 생성 담당
 * <p> DB 결과를 graph 객체로 조립한다.
 */
@Service
@RequiredArgsConstructor
public class GraphInitializationService {
    /**
     * repository에서 line/entrance 데이터를 가져온다.
     * CampusGraph를 생성한다.
     * 노드/엣지를 추가한다
     * buildingName -> entranceNodeIds 매핑을 생성한다.
     */
    private static final double EPS = 1e-6;

    private final NavigationRepository navigationRepository;
    private volatile CampusGraph campusGraph;


    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {

        long start = System.currentTimeMillis();

        CampusGraph graph = new CampusGraph();

        long lineStart = System.currentTimeMillis();
        loadBaseLines(graph);
        long lineEnd = System.currentTimeMillis();

        long entranceStart = System.currentTimeMillis();
        loadEntrances(graph);
        long entranceEnd = System.currentTimeMillis();

        this.campusGraph = graph;

        long end = System.currentTimeMillis();

        System.out.println("loadBaseLines ms = " + (lineEnd - lineStart));
        System.out.println("loadEntrances ms = " + (entranceEnd - entranceStart));
        System.out.println("graph init total ms = " + (end - start));
    }

    public CampusGraph getGraph() {
        if (campusGraph == null) {
            throw new IllegalStateException("그래프가 아직 초기화되지 않았습니다.");
        }
        return campusGraph;
    }

    private void loadBaseLines(CampusGraph graph) {
        List<LineSegmentRow> lines = navigationRepository.findAllWalkableLineSegments();

        for (LineSegmentRow row : lines) {
            long startNodeId = graph.getOrCreateBaseNode(row.startPoint());
            long endNodeId = graph.getOrCreateBaseNode(row.endPoint());

            graph.putLineEndpoints(row.lineId(), new LineEndpoints(startNodeId, endNodeId));

            graph.addDirectedEdge(startNodeId, endNodeId, row.cost(), row.highway(), row.geometry());
            graph.addDirectedEdge(endNodeId, startNodeId, row.cost(), row.highway(), reverse(row.geometry()));
        }
    }

    private void loadEntrances(CampusGraph graph) {
        List<EntranceProjectionRow> entrances = navigationRepository.findAllEntranceProjections();

        for (EntranceProjectionRow row : entrances) {
            long entranceNodeId = graph.createEntranceNode(
                    row.entrancePoint(),
                    row.entranceId(),
                    row.description()
            );

            graph.addBuildingEntrance(normalize(row.description()), entranceNodeId);

            LineEndpoints endpoints = graph.getLineEndpoints(row.lineId());
            if (endpoints == null) {
                throw new IllegalStateException("line endpoint를 찾을 수 없습니다. lineId=" + row.lineId());
            }

            if (row.fraction() <= EPS) {
                graph.addDirectedEdge(entranceNodeId, endpoints.startNodeId(), row.connectorCost(), "entrance_connector", row.connectorGeometry());
                graph.addDirectedEdge(endpoints.startNodeId(), entranceNodeId, row.connectorCost(), "entrance_connector", reverse(row.connectorGeometry()));
                continue;
            }

            if (row.fraction() >= 1.0 - EPS) {
                graph.addDirectedEdge(entranceNodeId, endpoints.endNodeId(), row.connectorCost(), "entrance_connector", row.connectorGeometry());
                graph.addDirectedEdge(endpoints.endNodeId(), entranceNodeId, row.connectorCost(), "entrance_connector", reverse(row.connectorGeometry()));
                continue;
            }

            long projectionNodeId = graph.createProjectionNode(row.projectionPoint());

            graph.addDirectedEdge(entranceNodeId, projectionNodeId, row.connectorCost(), "entrance_connector", row.connectorGeometry());
            graph.addDirectedEdge(projectionNodeId, entranceNodeId, row.connectorCost(), "entrance_connector", reverse(row.connectorGeometry()));

            if (row.leftCost() > EPS && !row.leftGeometry().isEmpty()) {
                graph.addDirectedEdge(endpoints.startNodeId(), projectionNodeId, row.leftCost(), "walkway", row.leftGeometry());
                graph.addDirectedEdge(projectionNodeId, endpoints.startNodeId(), row.leftCost(), "walkway", reverse(row.leftGeometry()));
            }

            if (row.rightCost() > EPS && !row.rightGeometry().isEmpty()) {
                graph.addDirectedEdge(projectionNodeId, endpoints.endNodeId(), row.rightCost(), "walkway", row.rightGeometry());
                graph.addDirectedEdge(endpoints.endNodeId(), projectionNodeId, row.rightCost(), "walkway", reverse(row.rightGeometry()));
            }
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
