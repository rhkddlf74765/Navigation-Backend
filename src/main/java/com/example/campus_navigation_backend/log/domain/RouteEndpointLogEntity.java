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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 라우팅 요청에 포함된 출발지와 목적지 원본 endpoint를 저장하는 로그 엔티티이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "route_endpoint_log",
        schema = "log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_route_endpoint_session_role",
                columnNames = {"route_session_id", "role"}
        )
)
public class RouteEndpointLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_session_id", nullable = false)
    private UUID routeSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private RouteEndpointRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "endpoint_type", nullable = false, length = 30)
    private RouteEndpointLogType endpointType;

    @Embedded
    private PointLog point;

    @Column(name = "building_name")
    private String buildingName;

    /**
     * 좌표 기반 endpoint 로그를 생성한다.
     */
    public static RouteEndpointLogEntity coordinate(
            UUID routeSessionId,
            RouteEndpointRole role,
            Double lon,
            Double lat,
            Double ele
    ) {
        RouteEndpointLogEntity entity = new RouteEndpointLogEntity();
        entity.routeSessionId = routeSessionId;
        entity.role = role;
        entity.endpointType = RouteEndpointLogType.COORDINATE;
        entity.point = PointLog.of(lon, lat, ele);
        return entity;
    }

    /**
     * 건물명 기반 endpoint 로그를 생성한다.
     */
    public static RouteEndpointLogEntity building(
            UUID routeSessionId,
            RouteEndpointRole role,
            String buildingName
    ) {
        RouteEndpointLogEntity entity = new RouteEndpointLogEntity();
        entity.routeSessionId = routeSessionId;
        entity.role = role;
        entity.endpointType = RouteEndpointLogType.BUILDING;
        entity.buildingName = buildingName;
        return entity;
    }
}
