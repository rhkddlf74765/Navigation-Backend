package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
 * 클라이언트가 보낸 GPS 추정 좌표와 실제 좌표를 저장하는 위치 로그 엔티티이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "location_sample_log", schema = "log")
public class LocationSampleLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "route_session_id")
    private UUID routeSessionId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lon", column = @Column(name = "gps_lon", nullable = false)),
            @AttributeOverride(name = "lat", column = @Column(name = "gps_lat", nullable = false)),
            @AttributeOverride(name = "ele", column = @Column(name = "gps_ele"))
    })
    private PointLog gps;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lon", column = @Column(name = "actual_lon")),
            @AttributeOverride(name = "lat", column = @Column(name = "actual_lat")),
            @AttributeOverride(name = "ele", column = @Column(name = "actual_ele"))
    })
    private PointLog actual;

    @Column(name = "error_meters")
    private Double errorMeters;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    /**
     * 위치 샘플 로그를 생성한다.
     *
     * @param userId 샘플을 전송한 사용자 ID
     * @param routeSessionId 연결된 라우팅 세션 ID
     * @param gps GPS 추정 좌표
     * @param actual 실제 기준 좌표
     * @param errorMeters GPS 추정 좌표와 실제 좌표 사이의 거리 오차
     * @param recordedAt 클라이언트가 위치를 기록한 시각
     * @param receivedAt 서버가 샘플을 수신한 시각
     * @return 위치 샘플 로그 엔티티
     */
    public static LocationSampleLogEntity create(
            UUID userId,
            UUID routeSessionId,
            PointLog gps,
            PointLog actual,
            Double errorMeters,
            Instant recordedAt,
            Instant receivedAt
    ) {
        LocationSampleLogEntity entity = new LocationSampleLogEntity();
        entity.userId = userId;
        entity.routeSessionId = routeSessionId;
        entity.gps = gps;
        entity.actual = actual;
        entity.errorMeters = errorMeters;
        entity.recordedAt = recordedAt;
        entity.receivedAt = receivedAt;
        return entity;
    }
}
