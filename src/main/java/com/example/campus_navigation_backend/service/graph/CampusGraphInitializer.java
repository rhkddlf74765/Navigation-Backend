package com.example.campus_navigation_backend.service.graph;

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

/**
 * 애플리케이션 시작 시 데이터베이스의 노드와 엣지를 읽어 메모리 캠퍼스 그래프를 초기화하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class CampusGraphInitializer {

    private final NavigationRepository navigationRepository;
    private final GradeAdjustedEdgeCostPolicy edgeCostPolicy;
    private final CampusGraphStore campusGraphStore;

    /**
     * 노드와 엣지를 순서대로 로딩해 불변 그래프를 만든 뒤 전역 그래프 저장소에 등록한다.
     */
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

    /**
     * 데이터베이스에서 읽은 노드를 그래프 빌더에 추가하고, 설명이 있는 노드는 건물 출입구 인덱스에도 등록한다.
     */
    private void loadNodes(CampusGraph.Builder builder) {
        List<GraphNodeRow> nodes = navigationRepository.findAllGraphNodes();

        for (GraphNodeRow row : nodes) {
            builder.addNode(row.id(), row.graphNodeType(), row.id(), row.point());
            if (row.description() != null && !row.description().isBlank()) {
                builder.addBuildingEntrance(normalize(row.description()), row.id());
            }
        }
    }

    /**
     * 데이터베이스에서 읽은 엣지를 방향성 그래프 엣지로 추가하고, 역방향 비용도 이동 방향 고도 차이에 맞춰 계산한다.
     */
    private void loadEdges(CampusGraph.Builder builder) {
        List<GraphEdgeRow> edges = navigationRepository.findAllGraphEdges();

        for (GraphEdgeRow row : edges) {
            GraphNode source = builder.getNode(row.source());
            GraphNode target = builder.getNode(row.target());
            if (source == null || target == null) {
                throw new IllegalStateException("Edge endpoint node not found. edgeId=" + row.id());
            }

            double elevationDelta = target.point().ele() - source.point().ele();
            double forwardCost = edgeCostPolicy.calculate(row.highway(), row.highway(), row.cost(), elevationDelta);
            double reverseCost = edgeCostPolicy.calculate(row.highway(), row.highway(), row.cost(), -elevationDelta);

            builder.addDirectedEdge(source.id(), target.id(), forwardCost, row.highway(), row.geometry());
            builder.addDirectedEdge(target.id(), source.id(), reverseCost, row.highway(), reverse(row.geometry()));
        }
    }

    /**
     * 역방향 엣지 geometry를 만들기 위해 원본 geometry의 좌표 순서를 뒤집는다.
     */
    private List<Point3D> reverse(List<Point3D> geometry) {
        List<Point3D> copied = new java.util.ArrayList<>(geometry);
        Collections.reverse(copied);
        return copied;
    }

    /**
     * 건물명 조회 시 공백과 대소문자 차이를 줄이기 위해 건물 설명 문자열을 정규화한다.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
