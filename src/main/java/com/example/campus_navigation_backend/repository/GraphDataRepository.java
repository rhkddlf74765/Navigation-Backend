package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;

import java.util.List;

/**
 * 서버 시작 시 CampusGraph 생성을 위해
 * PostgreSQL에서 그래프 원본 데이터를 읽어오는 포트.
 */
public interface GraphDataRepository {

    long findActiveGraphVersionId();

    List<GraphNodeRow> findAllGraphNodes(
            long graphVersionId
    );

    List<GraphEdgeRow> findAllGraphEdges(
            long graphVersionId
    );
}
