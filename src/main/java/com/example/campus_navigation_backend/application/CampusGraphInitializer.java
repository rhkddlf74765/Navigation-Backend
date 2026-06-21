package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.LineEndpoints;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.path.EdgeCostPolicy;
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
 * 애플리케이션 시작 시 DB의 보행로와 출입구 데이터를 읽어 CampusGraph를 생성한다.
 * 완성된 그래프는 CampusGraphStore에 한 번만 등록된다.
 */
@Service
@RequiredArgsConstructor
public class CampusGraphInitializer {

    private static final double EPS = 1e-6;

    private final NavigationRepository navigationRepository;
    private final EdgeCostPolicy edgeCostPolicy;
    private final CampusGraphStore campusGraphStore;

    /**
     * Spring Boot 애플리케이션 준비 완료 시점에 그래프를 초기화한다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        long start = System.currentTimeMillis();

        CampusGraph.Builder builder = CampusGraph.builder();

        long lineStart = System.currentTimeMillis();
        loadBaseLines(builder);
        long lineEnd = System.currentTimeMillis();

        long entranceStart = System.currentTimeMillis();
        loadEntrances(builder);
        long entranceEnd = System.currentTimeMillis();

        campusGraphStore.initialize(builder.build());

        long end = System.currentTimeMillis();

        System.out.println("loadBaseLines ms = " + (lineEnd - lineStart));
        System.out.println("loadEntrances ms = " + (entranceEnd - entranceStart));
        System.out.println("graph init total ms = " + (end - start));
    }

    /**
     * 보행 가능한 도로 선분을 그래프의 기본 노드와 양방향 엣지로 변환한다.
     * 각 엣지의 시작점과 끝점간의 고도 차를 이용하여 오르막길과 내리막길을 구분하여 실질적인 비용을 적용한다.
     *
     * @param builder 초기화 중인 그래프 빌더
     */
    private void loadBaseLines(CampusGraph.Builder builder) {
        List<LineSegmentRow> lines = navigationRepository.findAllWalkableLineSegments();

        for (LineSegmentRow row : lines) {
            long startNodeId = builder.getOrCreateBaseNode(row.startPoint());
            long endNodeId = builder.getOrCreateBaseNode(row.endPoint());

            builder.putLineEndpoints(row.lineId(), new LineEndpoints(startNodeId, endNodeId));

            double forwardCost = edgeCostPolicy.calculate("walkway", row.highway(), row.cost(), row.elevationDelta());
            double reverseCost = edgeCostPolicy.calculate("walkway", row.highway(), row.cost(), -row.elevationDelta());

            builder.addDirectedEdge(startNodeId, endNodeId, forwardCost, row.highway(), row.geometry());
            builder.addDirectedEdge(endNodeId, startNodeId, reverseCost, row.highway(), reverse(row.geometry()));
        }
    }

    /**
     * 건물 출입구를 그래프에 추가하고 가장 가까운 보행로와 연결한다.
     *
     * @param builder 초기화 중인 그래프 빌더
     */
    private void loadEntrances(CampusGraph.Builder builder) {
        List<EntranceProjectionRow> entrances = navigationRepository.findAllEntranceProjections();

        for (EntranceProjectionRow row : entrances) {
            long entranceNodeId = builder.createEntranceNode(
                    row.entrancePoint(),
                    row.entranceId(),
                    row.description()
            );

            builder.addBuildingEntrance(normalize(row.description()), entranceNodeId);

            LineEndpoints endpoints = builder.getLineEndpoints(row.lineId());
            if (endpoints == null) {
                throw new IllegalStateException("Line endpoint not found. lineId=" + row.lineId());
            }

            if (row.fraction() <= EPS) {
                addConnector(builder, entranceNodeId, endpoints.startNodeId(), row.connectorCost(), row.connectorGeometry());
                continue;
            }

            if (row.fraction() >= 1.0 - EPS) {
                addConnector(builder, entranceNodeId, endpoints.endNodeId(), row.connectorCost(), row.connectorGeometry());
                continue;
            }

            long projectionNodeId = builder.createProjectionNode(row.projectionPoint());

            addConnector(builder, entranceNodeId, projectionNodeId, row.connectorCost(), row.connectorGeometry());

            if (row.leftCost() > EPS && !row.leftGeometry().isEmpty()) {
                double leftElevationDelta = elevationDelta(row.leftGeometry());
                double leftForwardCost = edgeCostPolicy.calculate("walkway", row.lineHighway(), row.leftCost(), leftElevationDelta);
                double leftReverseCost = edgeCostPolicy.calculate("walkway", row.lineHighway(), row.leftCost(), -leftElevationDelta);
                builder.addDirectedEdge(endpoints.startNodeId(), projectionNodeId, leftForwardCost, row.lineHighway(), row.leftGeometry());
                builder.addDirectedEdge(projectionNodeId, endpoints.startNodeId(), leftReverseCost, row.lineHighway(), reverse(row.leftGeometry()));
            }

            if (row.rightCost() > EPS && !row.rightGeometry().isEmpty()) {
                double rightElevationDelta = elevationDelta(row.rightGeometry());
                double rightForwardCost = edgeCostPolicy.calculate("walkway", row.lineHighway(), row.rightCost(), rightElevationDelta);
                double rightReverseCost = edgeCostPolicy.calculate("walkway", row.lineHighway(), row.rightCost(), -rightElevationDelta);
                builder.addDirectedEdge(projectionNodeId, endpoints.endNodeId(), rightForwardCost, row.lineHighway(), row.rightGeometry());
                builder.addDirectedEdge(endpoints.endNodeId(), projectionNodeId, rightReverseCost, row.lineHighway(), reverse(row.rightGeometry()));
            }
        }
    }

    /**
     * 출입구 노드와 보행로 노드를 양방향 connector 엣지로 연결한다.
     *
     * @param builder 그래프 빌더
     * @param entranceNodeId 출입구 노드 ID
     * @param connectedNodeId 연결 대상 노드 ID
     * @param baseCost 보정 전 기본 거리 비용
     * @param geometry connector geometry
     */
    private void addConnector(CampusGraph.Builder builder,
                              long entranceNodeId,
                              long connectedNodeId,
                              double baseCost,
                              List<Point3D> geometry) {
        double elevationDelta = elevationDelta(geometry);
        double forwardCost = edgeCostPolicy.calculate("entrance_connector", "entrance_connector", baseCost, elevationDelta);
        double reverseCost = edgeCostPolicy.calculate("entrance_connector", "entrance_connector", baseCost, -elevationDelta);
        builder.addDirectedEdge(entranceNodeId, connectedNodeId, forwardCost, "entrance_connector", geometry);
        builder.addDirectedEdge(connectedNodeId, entranceNodeId, reverseCost, "entrance_connector", reverse(geometry));
    }

    /**
     * geometry의 마지막 좌표 고도와 첫 좌표 고도의 차이를 계산한다.
     *
     * @param geometry 고도 차이를 계산할 geometry
     * @return 마지막 좌표와 첫 좌표의 고도 차이
     */
    private double elevationDelta(List<Point3D> geometry) {
        if (geometry == null || geometry.size() < 2) {
            return 0.0;
        }

        Point3D start = geometry.get(0);
        Point3D end = geometry.get(geometry.size() - 1);
        return end.z() - start.z();
    }

    /**
     * 역방향 엣지 생성을 위해 geometry 좌표 순서를 뒤집는다.
     *
     * @param geometry 원본 geometry
     * @return 순서가 뒤집힌 geometry
     */
    private List<Point3D> reverse(List<Point3D> geometry) {
        List<Point3D> copied = new java.util.ArrayList<>(geometry);
        Collections.reverse(copied);
        return copied;
    }

    /**
     * 건물명 매칭을 위해 공백과 대소문자를 정규화한다.
     *
     * @param value 원본 문자열
     * @return 정규화된 문자열
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
