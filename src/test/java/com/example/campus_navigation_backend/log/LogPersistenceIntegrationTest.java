package com.example.campus_navigation_backend.log;

import com.example.campus_navigation_backend.api.location.dto.request.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.request.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.response.LocationSampleResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LogPersistenceIntegrationTest
        extends PostgisTestContainerSupport {

    private static final UUID TEST_USER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000101"
            );

    private static final UUID FAILED_ROUTE_USER_ID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000102"
            );

    @Autowired
    private CampusNavigationFacade campusNavigationFacade;

    @Autowired
    private LocationSampleService locationSampleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesRouteSessionAndLocationSampleLogsToDatabase() {
        RouteRequest routeRequest =
                new RouteRequest(
                        TEST_USER_ID,
                        RouteEndpointRequest.coordinate(
                                0.0002,
                                0.0001,
                                0.0
                        ),
                        RouteEndpointRequest.building(
                                "Test Building"
                        )
                );

        RouteResponse routeResponse =
                campusNavigationFacade.findRoute(
                        routeRequest
                );

        assertRouteSessionLog(
                routeResponse.routeSessionId()
        );

        LocationSampleRequest locationRequest =
                new LocationSampleRequest(
                        TEST_USER_ID,
                        routeResponse.routeSessionId(),
                        new LocationCoordinateRequest(
                                0.00021,
                                0.00011,
                                1.0
                        ),
                        new LocationCoordinateRequest(
                                0.00020,
                                0.00010,
                                1.5
                        ),
                        Instant.parse(
                                "2026-07-09T01:00:00Z"
                        )
                );

        LocationSampleResponse locationResponse =
                locationSampleService.save(
                        locationRequest
                );

        assertThat(locationResponse.locationRecordedAt())
                .isNotNull();
        assertThat(locationResponse.nearbyBuildings())
                .isNotNull();

        Long locationSampleLogId =
                findLocationSampleLogId(
                        routeResponse.routeSessionId()
                );

        assertThat(locationSampleLogId)
                .isNotNull();
        assertLocationSampleLog(
                locationSampleLogId,
                routeResponse.routeSessionId()
        );
    }

    @Test
    void savesFailedRouteStateToRouteSessionLog() {
        RouteRequest routeRequest =
                new RouteRequest(
                        FAILED_ROUTE_USER_ID,
                        RouteEndpointRequest.building(
                                "Test Building"
                        ),
                        RouteEndpointRequest.building(
                                "Missing Building"
                        )
                );

        assertThatThrownBy(
                () ->
                        campusNavigationFacade.findRoute(
                                routeRequest
                        )
        ).isInstanceOf(
                IllegalArgumentException.class
        );

        Map<String, Object> row =
                jdbcTemplate.queryForMap(
                        """
                        SELECT status,
                               end_reason,
                               requested_at,
                               responded_at,
                               ended_at,
                               request_json::text AS request_json,
                               error_message
                        FROM log.route_session_log
                        WHERE user_id = ?
                        ORDER BY requested_at DESC
                        LIMIT 1
                        """,
                        FAILED_ROUTE_USER_ID
                );

        assertThat(row.get("status"))
                .isEqualTo("ROUTE_FAILED");
        assertThat(row.get("end_reason"))
                .isEqualTo("ERROR");
        assertThat(row.get("requested_at"))
                .isNotNull();
        assertThat(row.get("responded_at"))
                .isNotNull();
        assertThat(row.get("ended_at"))
                .isNotNull();
        assertThat((String) row.get("request_json"))
                .contains(
                        "Missing Building"
                );
        assertThat(row.get("error_message"))
                .isNotNull();
    }

    private void assertRouteSessionLog(
            UUID routeSessionId
    ) {
        Map<String, Object> row =
                jdbcTemplate.queryForMap(
                        """
                        SELECT user_id,
                               status,
                               requested_at,
                               responded_at,
                               expected_time_seconds,
                               request_json::text AS request_json,
                               total_distance_meters,
                               total_cost,
                               path_json::text AS path_json
                        FROM log.route_session_log
                        WHERE route_session_id = ?
                        """,
                        routeSessionId
                );

        assertThat(row.get("user_id"))
                .isEqualTo(TEST_USER_ID);
        assertThat(row.get("status"))
                .isEqualTo("ROUTE_RETURNED");
        assertThat(row.get("requested_at"))
                .isNotNull();
        assertThat(row.get("responded_at"))
                .isNotNull();
        assertThat(row.get("request_json"))
                .isNotNull();
        assertThat((String) row.get("request_json"))
                .contains(
                        "start",
                        "destination",
                        "Test Building"
                );
        assertThat((Double) row.get("total_distance_meters"))
                .isGreaterThan(0.0);
        assertThat((Double) row.get("total_cost"))
                .isGreaterThan(0.0);
        assertThat(
                ((Number) row.get("expected_time_seconds"))
                        .longValue()
        ).isGreaterThanOrEqualTo(0L);
        assertThat(row.get("path_json"))
                .isNotNull();
        assertThat((String) row.get("path_json"))
                .contains(
                        "lon",
                        "lat",
                        "ele"
                );
    }

    private Long findLocationSampleLogId(
            UUID routeSessionId
    ) {
        return jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM log.location_sample_log
                WHERE route_session_id = ?
                ORDER BY id DESC
                LIMIT 1
                """,
                Long.class,
                routeSessionId
        );
    }

    private void assertLocationSampleLog(
            Long id,
            UUID routeSessionId
    ) {
        Map<String, Object> row =
                jdbcTemplate.queryForMap(
                        """
                        SELECT user_id,
                               route_session_id,
                               gps_lon,
                               gps_lat,
                               gps_ele,
                               actual_lon,
                               actual_lat,
                               actual_ele,
                               error_meters,
                               recorded_at,
                               received_at
                        FROM log.location_sample_log
                        WHERE id = ?
                        """,
                        id
                );

        assertThat(row.get("user_id"))
                .isEqualTo(TEST_USER_ID);
        assertThat(row.get("route_session_id"))
                .isEqualTo(routeSessionId);
        assertThat(row.get("gps_lon"))
                .isEqualTo(0.00021);
        assertThat(row.get("gps_lat"))
                .isEqualTo(0.00011);
        assertThat(row.get("gps_ele"))
                .isEqualTo(1.0);
        assertThat(row.get("actual_lon"))
                .isEqualTo(0.00020);
        assertThat(row.get("actual_lat"))
                .isEqualTo(0.00010);
        assertThat(row.get("actual_ele"))
                .isEqualTo(1.5);
        assertThat((Double) row.get("error_meters"))
                .isGreaterThan(0.0);
        assertThat(row.get("recorded_at"))
                .isNotNull();
        assertThat(row.get("received_at"))
                .isNotNull();
    }
}
