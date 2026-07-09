package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 라우팅 세션의 도착 확정 정보를 저장하는 로그 엔티티이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_arrival_log", schema = "log")
public class RouteArrivalLogEntity {

    @Id
    @Column(name = "route_session_id", nullable = false)
    private UUID routeSessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "arrived_at", nullable = false)
    private Instant arrivedAt;

    @Embedded
    private PointLog point;

    /**
     * 도착 확정 로그를 생성한다.
     */
    public static RouteArrivalLogEntity create(
            UUID routeSessionId,
            UUID userId,
            Instant arrivedAt,
            PointLog point
    ) {
        RouteArrivalLogEntity entity = new RouteArrivalLogEntity();
        entity.routeSessionId = routeSessionId;
        entity.userId = userId;
        entity.arrivedAt = arrivedAt;
        entity.point = point;
        return entity;
    }
}
