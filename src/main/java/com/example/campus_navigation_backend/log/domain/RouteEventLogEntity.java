package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 라우팅 세션 중 발생한 주요 이벤트를 시간순으로 저장하는 로그 엔티티이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_event_log", schema = "log")
public class RouteEventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_session_id", nullable = false)
    private UUID routeSessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private RouteEventType eventType;

    @Column(name = "event_message")
    private String eventMessage;

    @Embedded
    private PointLog point;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /**
     * 좌표가 없는 기본 이벤트 로그를 생성한다.
     */
    public static RouteEventLogEntity create(
            UUID routeSessionId,
            UUID userId,
            RouteEventType eventType,
            String eventMessage,
            Instant occurredAt
    ) {
        return create(routeSessionId, userId, eventType, eventMessage, null, occurredAt);
    }

    /**
     * 좌표를 포함하는 이벤트 로그를 생성한다.
     */
    public static RouteEventLogEntity create(
            UUID routeSessionId,
            UUID userId,
            RouteEventType eventType,
            String eventMessage,
            PointLog point,
            Instant occurredAt
    ) {
        RouteEventLogEntity entity = new RouteEventLogEntity();
        entity.routeSessionId = routeSessionId;
        entity.userId = userId;
        entity.eventType = eventType;
        entity.eventMessage = eventMessage;
        entity.point = point;
        entity.occurredAt = occurredAt;
        return entity;
    }
}
