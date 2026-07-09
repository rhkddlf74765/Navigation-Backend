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

import java.time.Instant;
import java.util.UUID;

/**
 * 라우팅 요청 1회의 생명주기와 상태를 저장하는 로그 엔티티이다.
 * <p>
 * 경로 요청 접수, 경로 반환, 안내 시작, 도착, 실패와 같은 세션 수준 상태를 기록한다.
 */
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

    @Column(name = "navigation_started_at")
    private Instant navigationStartedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "error_message")
    private String errorMessage;

    private RouteSessionLogEntity(UUID routeSessionId, UUID userId, Instant requestedAt) {
        this.routeSessionId = routeSessionId;
        this.userId = userId;
        this.status = RouteSessionStatus.REQUESTED;
        this.requestedAt = requestedAt;
    }

    public static RouteSessionLogEntity requested(UUID routeSessionId, UUID userId, Instant requestedAt) {
        return new RouteSessionLogEntity(routeSessionId, userId, requestedAt);
    }

    /**
     * 경로 계산이 성공적으로 끝났음을 기록한다.
     *
     * @param respondedAt 경로 응답이 생성된 시각
     */
    public void markRouteReturned(Instant respondedAt) {
        this.status = RouteSessionStatus.ROUTE_RETURNED;
        this.respondedAt = respondedAt;
    }

    /**
     * 경로 계산 실패 상태와 실패 메시지를 기록한다.
     *
     * @param errorMessage 실패 원인 메시지
     * @param respondedAt 실패 응답이 생성된 시각
     */
    public void markRouteFailed(String errorMessage, Instant respondedAt) {
        this.status = RouteSessionStatus.ROUTE_FAILED;
        this.errorMessage = errorMessage;
        this.respondedAt = respondedAt;
        this.endedAt = respondedAt;
        this.endReason = RouteEndReason.ERROR;
    }

    /**
     * 사용자가 반환된 경로 안내를 시작했음을 기록한다.
     *
     * @param startedAt 안내 시작 시각
     */
    public void startNavigation(Instant startedAt) {
        this.status = RouteSessionStatus.NAVIGATING;
        this.navigationStartedAt = startedAt;
    }

    /**
     * 목적지 도착으로 라우팅 세션이 종료되었음을 기록한다.
     *
     * @param arrivedAt 도착 확정 시각
     */
    public void markArrived(Instant arrivedAt) {
        this.status = RouteSessionStatus.ARRIVED;
        this.endedAt = arrivedAt;
        this.endReason = RouteEndReason.ARRIVED;
    }
}
