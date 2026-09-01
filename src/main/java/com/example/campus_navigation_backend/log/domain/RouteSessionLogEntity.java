package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_session_log", schema = "log")
public class RouteSessionLogEntity {

    @Id
    @Column(name = "route_session_id", nullable = false)
    private UUID routeSessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", length = 30)
    private RouteEndReason endReason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "expected_time_seconds")
    private Long expectedTimeSeconds;

    @Column(name = "error_message")
    private String errorMessage;

    /*
     * 클라이언트가 전달한 RouteRequest 원본.
     */
    @ColumnTransformer(
            read = "request_json::text",
            write = "?::jsonb"
    )
    @Column(
            name = "request_json",
            columnDefinition = "jsonb"
    )
    private String requestJson;

    /*
     * 실제 이동 거리.
     */
    @Column(name = "total_distance_meters")
    private Double totalDistanceMeters;

    /*
     * A* 및 EdgeCostPolicy가 사용한 전체 routing cost.
     */
    @Column(name = "total_cost")
    private Double totalCost;

    /*
     * 최종적으로 클라이언트에 반환한 RoutePoint 배열.
     */
    @ColumnTransformer(
            read = "path_json::text",
            write = "?::jsonb"
    )
    @Column(
            name = "path_json",
            columnDefinition = "jsonb"
    )
    private String pathJson;

    private RouteSessionLogEntity(
            UUID routeSessionId,
            UUID userId,
            Instant requestedAt,
            String requestJson
    ) {
        this.routeSessionId = routeSessionId;
        this.userId = userId;
        this.status = RouteSessionStatus.REQUESTED;
        this.requestedAt = requestedAt;
        this.requestJson = requestJson;
    }

    public static RouteSessionLogEntity requested(
            UUID routeSessionId,
            UUID userId,
            Instant requestedAt,
            String requestJson
    ) {
        return new RouteSessionLogEntity(
                routeSessionId,
                userId,
                requestedAt,
                requestJson
        );
    }

    public void markRouteReturned(
            double totalDistanceMeters,
            double totalCost,
            long expectedTimeSeconds,
            String pathJson,
            Instant respondedAt
    ) {
        this.status = RouteSessionStatus.ROUTE_RETURNED;
        this.respondedAt = respondedAt;

        this.totalDistanceMeters = totalDistanceMeters;
        this.totalCost = totalCost;
        this.expectedTimeSeconds = expectedTimeSeconds;
        this.pathJson = pathJson;

        this.errorMessage = null;
    }

    public void markRouteFailed(
            String errorMessage,
            Instant respondedAt
    ) {
        this.status = RouteSessionStatus.ROUTE_FAILED;
        this.errorMessage = errorMessage;

        this.respondedAt = respondedAt;
        this.endedAt = respondedAt;
        this.endReason = RouteEndReason.ERROR;
    }

    public void startNavigation() {
        this.status = RouteSessionStatus.NAVIGATING;
    }

    public void markArrived(
            Instant arrivedAt
    ) {
        this.status = RouteSessionStatus.ARRIVED;
        this.endedAt = arrivedAt;
        this.endReason = RouteEndReason.ARRIVED;
    }

    public void markCancelled(
            Instant cancelledAt
    ) {
        this.status = RouteSessionStatus.CANCELLED;
        this.endedAt = cancelledAt;
        this.endReason = RouteEndReason.USER_CANCELLED;
    }

    public void markExpired(
            Instant expiredAt
    ) {
        this.status = RouteSessionStatus.EXPIRED;
        this.endedAt = expiredAt;
        this.endReason = RouteEndReason.TIMEOUT;
    }
}