package com.example.campus_navigation_backend.service.location;

import com.example.campus_navigation_backend.api.location.dto.request.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.request.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.response.LocationSampleResponse;
import com.example.campus_navigation_backend.api.location.dto.response.NearbyBuildingResponse;
import com.example.campus_navigation_backend.application.nearbyBuilding.NearbyBuildingService;
import com.example.campus_navigation_backend.log.service.LocationLogService;
import com.example.campus_navigation_backend.service.spatial.UserSpatialStateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 위치 샘플 저장 use case를 처리하는 서비스이다.
 * <p>
 * GPS 추정 좌표와 실제 기준 좌표가 함께 전달된 경우 두 좌표 사이의 거리 오차를 계산한 뒤 저장소에 기록한다.
 */
@Service
@RequiredArgsConstructor
public class LocationSampleService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LocationSampleService.class
            );

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final LocationLogService locationLogService;

    private final UserSpatialStateService
            userSpatialStateService;

    private final NearbyBuildingService nearbyBuildingService;

    /**
     * 위치 샘플을 저장하고, 실제 좌표가 있는 경우 GPS 오차를 계산해 응답에 포함한다.
     */
    public LocationSampleResponse save(LocationSampleRequest request) {
        Instant receivedAt = Instant.now();
        Double errorMeters = calculateErrorMeters(request.gps(), request.actual());

        try {
            locationLogService.save(
                    request,
                    errorMeters,
                    receivedAt
            );
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to save location sample log. userId={}",
                    request.userId(),
                    e
            );
        }

        Instant observedAt =
                request.recordedAt() == null
                        ? receivedAt
                        : request.recordedAt();

        try {
            userSpatialStateService
                    .updateFromGps(
                            request.userId(),
                            request.gps().lat(),
                            request.gps().lon(),
                            observedAt
                    );
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to update user spatial state. userId={}",
                    request.userId(),
                    e
            );
        }

        List<NearbyBuildingResponse>
                nearbyBuildings =
                findNearbyBuildings(
                        request
                );

        return new LocationSampleResponse(
                receivedAt,
                nearbyBuildings
        );
    }

    private List<NearbyBuildingResponse>
    findNearbyBuildings(
            LocationSampleRequest request
    ) {
        try {
            return nearbyBuildingService
                    .findNearby(
                            request.gps().lat(),
                            request.gps().lon()
                    )
                    .stream()
                    .map(
                            NearbyBuildingResponse::from
                    )
                    .toList();
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to query nearby buildings. userId={}",
                    request.userId(),
                    e
            );

            return List.of();
        }
    }

    /**
     * 실제 기준 좌표가 있는 경우에만 GPS 좌표와 실제 좌표 사이의 거리 오차를 계산한다.
     */
    private Double calculateErrorMeters(LocationCoordinateRequest gps, LocationCoordinateRequest actual) {
        if (actual == null) {
            return null;
        }

        return haversineMeters(
                gps.lat(),
                gps.lon(),
                actual.lat(),
                actual.lon()
        );
    }

    /**
     * 위경도 두 점 사이의 지표면 근사 거리를 계산해 GPS 오차를 미터 단위로 저장할 수 있게 한다.
     */
    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
