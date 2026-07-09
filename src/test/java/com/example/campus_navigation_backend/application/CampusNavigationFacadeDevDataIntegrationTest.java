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
 * Verifies the facade against real development data loaded from the dev-db test profile.
 */
class CampusNavigationFacadeDevDataIntegrationTest extends DevDatabaseIntegrationTestSupport {

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
     * Ensures the facade can resolve a route from the configured dev start point to the configured destination building.
     * The test focuses on the full routing flow: request parsing, projection, destination lookup, and route assembly.
     */
    @Test
    void findsRouteUsingConfiguredDevScenario() {
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


