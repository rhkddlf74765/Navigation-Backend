package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.UUID;

/**
 * 성공적으로 계산된 경로 결과 요약을 저장하는 로그 엔티티이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_result_log", schema = "log")
public class RouteResultLogEntity {

    @Id
    @Column(name = "route_session_id", nullable = false)
    private UUID routeSessionId;

    @Column(name = "destination_building_name")
    private String destinationBuildingName;

    @Column(name = "selected_entrance_id")
    private Long selectedEntranceId;

    @Column(name = "total_distance_meters", nullable = false)
    private double totalDistanceMeters;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @Column(name = "approach_distance_meters", nullable = false)
    private double approachDistanceMeters;

    @Column(name = "approach_cost", nullable = false)
    private double approachCost;

    @Column(name = "graph_distance_meters", nullable = false)
    private double graphDistanceMeters;

    @Column(name = "graph_cost", nullable = false)
    private double graphCost;

    @ColumnTransformer(read = "path::text", write = "?::jsonb")
    @Column(name = "path", nullable = false, columnDefinition = "jsonb")
    private String path;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * 경로 응답 내용을 기반으로 결과 로그를 생성한다.
     */
    public static RouteResultLogEntity create(
            UUID routeSessionId,
            String destinationBuildingName,
            long selectedEntranceId,
            double totalDistanceMeters,
            double totalCost,
            double approachDistanceMeters,
            double approachCost,
            double graphDistanceMeters,
            double graphCost,
            String path,
            Instant createdAt
    ) {
        RouteResultLogEntity entity = new RouteResultLogEntity();
        entity.routeSessionId = routeSessionId;
        entity.destinationBuildingName = destinationBuildingName;
        entity.selectedEntranceId = selectedEntranceId;
        entity.totalDistanceMeters = totalDistanceMeters;
        entity.totalCost = totalCost;
        entity.approachDistanceMeters = approachDistanceMeters;
        entity.approachCost = approachCost;
        entity.graphDistanceMeters = graphDistanceMeters;
        entity.graphCost = graphCost;
        entity.path = path;
        entity.createdAt = createdAt;
        return entity;
    }
}
