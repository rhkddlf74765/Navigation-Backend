package com.example.campus_navigation_backend.log;

import com.example.campus_navigation_backend.api.location.dto.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleResponse;
import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
import com.example.campus_navigation_backend.service.location.LocationSampleService;
import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 PostGIS 테스트 DB에 로그 엔티티가 저장되는지 검증하는 통합 테스트이다.
 */
@SpringBootTest
class LogPersistenceIntegrationTest extends PostgisTestContainerSupport {

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Autowired
    private CampusNavigationFacade campusNavigationFacade;

    @Autowired
    private LocationSampleService locationSampleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 라우팅 성공 로그와 위치 샘플 로그가 log 스키마의 실제 테이블에 저장되는지 확인한다.
     */
    @Test
    void savesRouteAndLocationLogsToDatabase() {
        RouteRequest routeRequest = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.coordinate(0.0002, 0.0001, 0.0),
                RouteEndpointRequest.building("Test Building")
        );

        RouteResponse routeResponse = campusNavigationFacade.findRoute(routeRequest);

        assertRouteSessionLog(routeResponse.routeSessionId());
        assertRouteEndpointLogs(routeResponse.routeSessionId());
        assertRouteResultLog(routeResponse.routeSessionId());
        assertRouteEventLog(routeResponse.routeSessionId());

        LocationSampleRequest locationRequest = new LocationSampleRequest(
                TEST_USER_ID,
                routeResponse.routeSessionId(),
                new LocationCoordinateRequest(0.00021, 0.00011, 1.0),
                new LocationCoordinateRequest(0.00020, 0.00010, 1.5),
                Instant.parse("2026-07-09T01:00:00Z")
        );

        LocationSampleResponse locationResponse = locationSampleService.save(locationRequest);

        assertThat(locationResponse.id()).isNotNull();
        assertThat(locationResponse.errorMeters()).isGreaterThan(0.0);
        assertLocationSampleLog(locationResponse.id(), routeResponse.routeSessionId());
    }

    private void assertRouteSessionLog(UUID routeSessionId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT user_id, status, requested_at, responded_at
                FROM log.route_session_log
                WHERE route_session_id = ?
                """,
                routeSessionId
        );

        assertThat(row.get("user_id")).isEqualTo(TEST_USER_ID);
        assertThat(row.get("status")).isEqualTo("ROUTE_RETURNED");
        assertThat(row.get("requested_at")).isNotNull();
        assertThat(row.get("responded_at")).isNotNull();
    }

    private void assertRouteEndpointLogs(UUID routeSessionId) {
        Integer endpointCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM log.route_endpoint_log WHERE route_session_id = ?",
                Integer.class,
                routeSessionId
        );
        assertThat(endpointCount).isEqualTo(2);

        Map<String, Object> start = jdbcTemplate.queryForMap(
                """
                SELECT endpoint_type, lon, lat, ele
                FROM log.route_endpoint_log
                WHERE route_session_id = ? AND role = 'START'
                """,
                routeSessionId
        );
        assertThat(start.get("endpoint_type")).isEqualTo("COORDINATE");
        assertThat(start.get("lon")).isEqualTo(0.0002);
        assertThat(start.get("lat")).isEqualTo(0.0001);
        assertThat(start.get("ele")).isEqualTo(0.0);

        Map<String, Object> destination = jdbcTemplate.queryForMap(
                """
                SELECT endpoint_type, building_name
                FROM log.route_endpoint_log
                WHERE route_session_id = ? AND role = 'DESTINATION'
                """,
                routeSessionId
        );
        assertThat(destination.get("endpoint_type")).isEqualTo("BUILDING");
        assertThat(destination.get("building_name")).isEqualTo("Test Building");
    }

    private void assertRouteResultLog(UUID routeSessionId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT destination_building_name, selected_entrance_id, total_distance_meters, path::text AS path
                FROM log.route_result_log
                WHERE route_session_id = ?
                """,
                routeSessionId
        );

        assertThat(row.get("destination_building_name")).isEqualTo("Test Building");
        assertThat(row.get("selected_entrance_id")).isNotNull();
        assertThat((Double) row.get("total_distance_meters")).isGreaterThan(0.0);
        assertThat((String) row.get("path")).contains("lon", "lat", "ele");
    }

    private void assertRouteEventLog(UUID routeSessionId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT user_id, event_type
                FROM log.route_event_log
                WHERE route_session_id = ?
                """,
                routeSessionId
        );

        assertThat(row.get("user_id")).isEqualTo(TEST_USER_ID);
        assertThat(row.get("event_type")).isEqualTo("ROUTE_RETURNED");
    }

    private void assertLocationSampleLog(Long id, UUID routeSessionId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT user_id, route_session_id, gps_lon, gps_lat, gps_ele,
                       actual_lon, actual_lat, actual_ele, error_meters, recorded_at, received_at
                FROM log.location_sample_log
                WHERE id = ?
                """,
                id
        );

        assertThat(row.get("user_id")).isEqualTo(TEST_USER_ID);
        assertThat(row.get("route_session_id")).isEqualTo(routeSessionId);
        assertThat(row.get("gps_lon")).isEqualTo(0.00021);
        assertThat(row.get("gps_lat")).isEqualTo(0.00011);
        assertThat(row.get("gps_ele")).isEqualTo(1.0);
        assertThat(row.get("actual_lon")).isEqualTo(0.00020);
        assertThat(row.get("actual_lat")).isEqualTo(0.00010);
        assertThat(row.get("actual_ele")).isEqualTo(1.5);
        assertThat((Double) row.get("error_meters")).isGreaterThan(0.0);
        assertThat(row.get("recorded_at")).isNotNull();
        assertThat(row.get("received_at")).isNotNull();
    }
}
