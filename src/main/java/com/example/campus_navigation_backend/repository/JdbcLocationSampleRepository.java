package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.api.location.dto.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleSource;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * 위치 샘플 로그를 JDBC로 저장하는 저장소 구현체이다.
 * <p>
 * 이 클래스는 스키마를 생성하지 않고, 미리 생성된 location_sample_log 테이블에 insert만 수행한다.
 */
@Repository
@RequiredArgsConstructor
public class JdbcLocationSampleRepository implements LocationSampleRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * GPS 좌표, 선택적 실제 좌표, 계산된 오차, 수집 시각을 location_sample_log 테이블에 저장한다.
     */
    @Override
    public Long save(LocationSampleRequest request, Double errorMeters, Instant receivedAt) {
        String sql = """
                INSERT INTO location_sample_log (
                    route_session_id,
                    source,
                    gps_longitude,
                    gps_latitude,
                    gps_altitude,
                    gps_accuracy_meters,
                    actual_longitude,
                    actual_latitude,
                    actual_altitude,
                    error_meters,
                    recorded_at,
                    received_at
                ) VALUES (
                    :routeSessionId,
                    :source,
                    :gpsLongitude,
                    :gpsLatitude,
                    :gpsAltitude,
                    :gpsAccuracyMeters,
                    :actualLongitude,
                    :actualLatitude,
                    :actualAltitude,
                    :errorMeters,
                    :recordedAt,
                    :receivedAt
                )
                RETURNING id
                """;

        Long id = jdbc.queryForObject(sql, params(request, errorMeters, receivedAt), Long.class);
        if (id == null) {
            throw new IllegalStateException("위치 샘플 로그 ID를 가져오지 못했습니다.");
        }
        return id;
    }

    /**
     * SQL insert에 사용할 파라미터를 구성하고, 누락 가능한 값은 null로 전달한다.
     */
    private MapSqlParameterSource params(LocationSampleRequest request, Double errorMeters, Instant receivedAt) {
        LocationCoordinateRequest gps = request.gps();
        LocationCoordinateRequest actual = request.actual();
        LocationSampleSource source = request.source() == null
                ? LocationSampleSource.ROUTE_TRACKING
                : request.source();

        return new MapSqlParameterSource()
                .addValue("routeSessionId", request.routeSessionId())
                .addValue("source", source.name())
                .addValue("gpsLongitude", gps.longitude())
                .addValue("gpsLatitude", gps.latitude())
                .addValue("gpsAltitude", gps.altitude())
                .addValue("gpsAccuracyMeters", gps.accuracyMeters())
                .addValue("actualLongitude", actual == null ? null : actual.longitude())
                .addValue("actualLatitude", actual == null ? null : actual.latitude())
                .addValue("actualAltitude", actual == null ? null : actual.altitude())
                .addValue("errorMeters", errorMeters)
                .addValue("recordedAt", request.recordedAt())
                .addValue("receivedAt", receivedAt);
    }
}
