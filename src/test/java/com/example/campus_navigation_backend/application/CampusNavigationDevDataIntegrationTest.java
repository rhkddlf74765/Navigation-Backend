package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
import com.example.campus_navigation_backend.support.DevDatabaseIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 개발 DB 데이터로 사용자 경로 탐색 흐름이 동작하는지 검증하는 통합 테스트이다.
 */
class CampusNavigationDevDataIntegrationTest extends DevDatabaseIntegrationTestSupport {

    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private CampusNavigationFacade campusNavigationFacade;

    @Value("${dev.test.start-lon}")
    private double startLon;

    @Value("${dev.test.start-lat}")
    private double startLat;

    @Value("${dev.test.start-ele}")
    private double startEle;

    @Value("${dev.test.destination-building}")
    private String destinationBuildingName;

    /**
     * 개발 DB의 실제 좌표와 실제 목적지 건물명으로 최종 경로 응답이 생성되는지 검증한다.
     */
    @Test
    void findsRouteUsingKnownDevDataScenario() {
        RouteRequest request = new RouteRequest(
                TEST_USER_ID,
                RouteEndpointRequest.coordinate(startLon, startLat, startEle),
                RouteEndpointRequest.building(destinationBuildingName)
        );

        RouteResponse response = campusNavigationFacade.findRoute(request);

        assertThat(response.destinationBuildingName()).isEqualTo(destinationBuildingName);
        assertThat(response.selectedEntranceId()).isGreaterThan(0);
        assertThat(response.totalDistanceMeters()).isGreaterThan(0.0);
        assertThat(response.path()).isNotEmpty();
    }
}
